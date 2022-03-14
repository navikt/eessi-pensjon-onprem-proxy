package no.nav.eessi.pensjon.config

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.security.sts.SecurityTokenResponse
import no.nav.eessi.pensjon.security.sts.UsernameToOidcInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Configuration
class RestTemplateConfig(private val securityTokenExchangeService: STSService) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(RestTemplateConfig::class.java) }

    @Value("\${NORG2_URL}")
    lateinit var norg2Url: String

    @Value("\${PENSJONSINFORMASJON_URL}")
    lateinit var peninfourl: String

    @Value("\${PENSJON_BEHANDLEHENDELSE_URL}")
    lateinit var penBeandleHendelseurl: String

    @Value("\${BESTEMSAK_URL}")
    lateinit var bestemSakUrl: String

    @Value("\${srvfagmodulpassword}")
    lateinit var srvFagmodulPassword: String

    @Value("\${srvfagmodulusername}")
    lateinit var srvFagmodulUsername: String

    @Value("\${srvpdlppassword}")
    lateinit var srvpdlppassword: String

    @Value("\${srvpdlpusername}")
    lateinit var srvpdlpusername: String

    @Value("\${PDL_PERSON_MOTTAK_URL}")
    lateinit var pdlMottakUrl: String

    @Bean
    fun personMottakRestTemplate() = buildRestTemplateCustomSystemUser(pdlMottakUrl, srvpdlpusername, srvpdlppassword)

    @Bean
    fun norg2OidcRestTemplate() = buildRestTemplate(norg2Url)

    @Bean
    fun pensjonInformasjonRestTemplate() = buildRestTemplateCustomSystemUser(peninfourl, srvFagmodulUsername, srvFagmodulPassword)

    @Bean
    fun behandleHendelseRestTemplate() = buildRestTemplateCustomSystemUser(penBeandleHendelseurl, srvFagmodulUsername, srvFagmodulPassword)

    @Bean
    fun bestemSakOidcRestTemplate() = buildRestTemplate(bestemSakUrl)


    private fun buildRestTemplate(url: String): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(url)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                UsernameToOidcInterceptor(securityTokenExchangeService))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory().apply { setOutputStreaming(false)})
            }

    }

    private fun buildRestTemplateCustomSystemUser(url: String, username: String, password: String): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(url)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                CustomUsernameToOidcInterceptor(username, password, securityTokenExchangeService)
            )
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory().apply { setOutputStreaming(false)})
            }.also { logger.info("username: $username, url: $url") }
    }

    private inner class CustomUsernameToOidcInterceptor(private val username: String, private val password: String, private val securityTokenExchangeService: STSService) : ClientHttpRequestInterceptor {
        private val logger = LoggerFactory.getLogger(CustomUsernameToOidcInterceptor::class.java)

        override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
            val token = getCustomSystemOidcToken(username, password)

            request.headers.setBearerAuth(token)
            request.headers["Nav-Consumer-Token"] = "Bearer $token"

            return execution.execute(request, body)
        }

        fun getCustomSystemOidcToken(username: String, password: String): String {
           return try {
                val uri = UriComponentsBuilder.fromUriString(securityTokenExchangeService.wellKnownSTS.tokenEndpoint)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .build().toUriString()

                logger.debug("Kaller STS for å bytte username/password til OIDC token")

                val response = customSecurityTokenExchangeBasicAuthRestTemplate(username, password).getForObject(
                    uri,
                    SecurityTokenResponse::class.java
                )
                logger.debug("*** CustomSecurityTokenResponse: ${response!!.accessToken}")
                response!!.accessToken

            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under bytting av username/password til OIDC token: ", ex)
                throw RuntimeException("En feil oppstod under bytting av username/password til OIDC token: ", ex)
            } catch (ex: Exception) {
                logger.error("En feil oppstod under bytting av username/password til OIDC token ex: ", ex)
                throw RuntimeException("En feil oppstod under bytting av username/password til OIDC token: ", ex)
            }
        }

        fun customSecurityTokenExchangeBasicAuthRestTemplate(username: String, password: String): RestTemplate {
            logger.info("Oppretter RestTemplate for securityTokenExchangeBasicAuthRestTemplate")
            return RestTemplateBuilder()
                .additionalInterceptors(
                    RequestIdHeaderInterceptor(),
                    BasicAuthenticationInterceptor(username, password)
                ).build()
        }
    }
}

