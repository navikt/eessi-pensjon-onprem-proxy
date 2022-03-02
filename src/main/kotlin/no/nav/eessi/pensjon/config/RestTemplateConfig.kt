package no.nav.eessi.pensjon.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.metrics.RequestCountInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.security.sts.UsernameToOidcInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.*
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.util.*

@Configuration
class RestTemplateConfig(private val securityTokenExchangeService: STSService) {

    @Value("\${NORG2_URL}")
    lateinit var norg2Url: String

    @Value("\${PENSJONSINFORMASJON_URL}")
    lateinit var peninfourl: String

    @Value("\${PENSJON_BEHANDLEHENDELSE_URL}")
    lateinit var penBeandleHendelseurl: String

    @Value("\${BESTEMSAK_URL}")
    lateinit var bestemSakUrl: String

    @Bean
    fun norg2OidcRestTemplate() = buildRestTemplate(norg2Url)

    @Bean
    fun pensjonInformasjonRestTemplate() = buildRestTemplate(peninfourl)

    @Bean
    fun behandleHendelseRestTemplate() = buildRestTemplate(penBeandleHendelseurl)

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

    @Bean
    fun bestemSakOidcRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
            .rootUri(bestemSakUrl)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                UsernameToOidcInterceptor(securityTokenExchangeService))
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
            }
    }
}

