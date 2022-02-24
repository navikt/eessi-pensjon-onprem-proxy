package no.nav.eessi.pensjon.klienter.norg2

import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import javax.annotation.PostConstruct

@Component
class Norg2Klient(private val norg2OidcRestTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(Norg2Klient::class.java)

    fun hentArbeidsfordelingEnheter(request: String): String {
        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val httpEntity = HttpEntity(request, headers)

            logger.info("Kaller NORG med : $request")
            val responseEntity = norg2OidcRestTemplate.exchange(
                "/api/v1/arbeidsfordeling",
                HttpMethod.POST,
                httpEntity,
                String::class.java
            )
            responseEntity.body!!
        } catch (ex: Exception) {
            throw ex
        }
    }
}

