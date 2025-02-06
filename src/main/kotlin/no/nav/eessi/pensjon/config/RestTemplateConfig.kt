package no.nav.eessi.pensjon.config

import com.nimbusds.jwt.JWTClaimsSet
import io.micrometer.core.instrument.MeterRegistry
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.eessi.pensjon.errorhandler.PenErrorHandler
import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.metrics.RequestCountInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.security.sts.SecurityTokenResponse
import no.nav.eessi.pensjon.security.sts.UsernameToOidcInterceptor
import no.nav.eessi.pensjon.shared.retry.IOExceptionRetryInterceptor
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.*
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*

@Configuration
class RestTemplateConfig(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val securityTokenExchangeService: STSService,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(RestTemplateConfig::class.java)
    private val secureLog = LoggerFactory.getLogger("secureLog")

    @Value("\${AZURE_APP_PEN_CLIENT_ID}")
    lateinit var pensjonClientId: String

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

    @Value("\${FAGMODUL_URL}")
    lateinit var fagmodulURL: String

    @Bean
    fun bestemSakOidcRestTemplate() = buildRestTemplate(bestemSakUrl)

    @Bean
    fun pensjonInformasjonRestTemplate() = opprettRestTemplate(peninfourl, onBehalfOfBearerTokenInterceptor(pensjonClientId), PenErrorHandler())

    @Bean
    fun behandleHendelseRestTemplate() = restTemplate(penBeandleHendelseurl, CustomUsernameToOidcInterceptor(srvFagmodulUsername, srvFagmodulPassword, securityTokenExchangeService))

    @Bean
    fun fagmodulOAuth2RestTemplate() = restTemplate(fagmodulURL, oAuth2BearerTokenInterceptor(
        clientConfigurationProperties.registration["fagmodul-credentials"]
            ?: throw RuntimeException("could not find oauth2 client config for ${"fagmodul-credentials"}"),
        oAuth2AccessTokenService
    ))

    private fun onBehalfOfBearerTokenInterceptor(clientId: String): ClientHttpRequestInterceptor {
        logger.info("init onBehalfOfBearerTokenInterceptor: $clientId")
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val decodedToken = URLDecoder.decode(getToken(tokenValidationContextHolder).encodedToken, StandardCharsets.UTF_8)

            secureLog.info("NAVIdent: ${getClaims(tokenValidationContextHolder).get("NAVident")?.toString()}")

            val tokenClient: AzureAdOnBehalfOfTokenClient = AzureAdTokenClientBuilder.builder()
                .withNaisDefaults()
                .buildOnBehalfOfTokenClient()

            val accessToken: String = tokenClient.exchangeOnBehalfOfToken(
                "api://$clientId/.default",
                decodedToken
            )
            logger.debug("accessToken: $accessToken")

            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }
    }

    private fun buildRestTemplate(url: String): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(url)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                IOExceptionRetryInterceptor(),
                RequestCountInterceptor(meterRegistry),
                RequestResponseLoggerInterceptor(),
                UsernameToOidcInterceptor(securityTokenExchangeService))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }

    }

    private fun restTemplate(url: String, tokenIntercetor: ClientHttpRequestInterceptor?) : RestTemplate {
        return RestTemplateBuilder()
            .rootUri(url)
            .errorHandler(DefaultResponseErrorHandler())
            .readTimeout(Duration.ofSeconds(120))
            .connectTimeout(Duration.ofSeconds(120))
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                IOExceptionRetryInterceptor(),
                RequestCountInterceptor(meterRegistry),
                RequestResponseLoggerInterceptor(),
                tokenIntercetor
            )
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    private fun opprettRestTemplate(url: String, tokenInterceptor: ClientHttpRequestInterceptor?, defaultErrorHandler: ResponseErrorHandler = DefaultResponseErrorHandler()) : RestTemplate {
        logger.info("init restTemplate: $url")
        return RestTemplateBuilder()
            .rootUri(url)
            .errorHandler(defaultErrorHandler)
            .readTimeout(Duration.ofSeconds(120))
            .connectTimeout(Duration.ofSeconds(120))
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                IOExceptionRetryInterceptor(),
                RequestCountInterceptor(meterRegistry),
                RequestResponseLoggerInterceptor(),
                tokenInterceptor
            )
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }

    private fun clientProperties(oAuthKey: String): ClientProperties {
        return Optional.ofNullable(clientConfigurationProperties.registration[oAuthKey])
            .orElseThrow { RuntimeException("could not find oauth2 client config for example-onbehalfof") }
    }

    private fun bearerTokenInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            val tokenChunks = response.access_token!!.split(".")
            val tokenBody =  tokenChunks[1]
            logger.debug("subject: " + JWTClaimsSet.parse(Base64.getDecoder().decode(tokenBody).decodeToString()).subject + "/n + $response.accessToken")

            request.headers.setBearerAuth(response.access_token!!)
            execution.execute(request, body!!)
        }
    }
    private fun oAuth2BearerTokenInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            request.headers.setBearerAuth(response.access_token!!)
            execution.execute(request, body!!)
        }
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
                val accessToken = response?.accessToken
                logger.debug("*** CustomSecurityTokenResponse: $accessToken")
                accessToken!!

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
                    IOExceptionRetryInterceptor(),
                    RequestCountInterceptor(meterRegistry),
                    BasicAuthenticationInterceptor(username, password)
                ).build()
        }
    }

    fun getClaims(tokenValidationContextHolder: TokenValidationContextHolder): JwtTokenClaims {
        val context = tokenValidationContextHolder.getTokenValidationContext()
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")

        val validIssuer = context.issuers.filterNot { issuer ->
            val oidcClaims = context.getClaims(issuer)
            oidcClaims.expirationTime.before(Date())
        }.map { it }


        if (validIssuer.isNotEmpty()) {
            val issuer = validIssuer.first()
            return context.getClaims(issuer)
        }
        throw RuntimeException("No valid issuer found in context")

    }

    fun getToken(tokenValidationContextHolder: TokenValidationContextHolder): JwtToken {
        val context = tokenValidationContextHolder.getTokenValidationContext()
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")
        val issuer = context.issuers.first()

        return context.getJwtToken(issuer)!!
    }

}

