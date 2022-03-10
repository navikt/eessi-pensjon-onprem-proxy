package no.nav.eessi.pensjon.pdlmottak

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class PersonMottakKlient(private val personMottakRestTemplate: RestTemplate) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PersonMottakKlient::class.java) }

    internal fun opprettPersonopplysning(personopplysning: String): Boolean {
        logger.debug("Oppretter endringsmelding med nye personopplysninger $personopplysning")

        val httpEntity = HttpEntity(personopplysning, createHeaders())

        val response = personMottakRestTemplate.exchange(
            "/api/v1/endringer",
            HttpMethod.POST,
            httpEntity,
            String::class.java
        )
        logger.info("Endringresponse StatusCode: ${response.statusCode}, Body: ${response.body}")
        return response.statusCode.is2xxSuccessful
    }

    private fun createHeaders(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.add("Nav-Call-Id", UUID.randomUUID().toString())
        httpHeaders.add("Tema", "PEN")
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
        return httpHeaders
    }

}
