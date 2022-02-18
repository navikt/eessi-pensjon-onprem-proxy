package no.nav.eessi.pensjon.kodeverk

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.utils.toJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.annotation.PostConstruct

@Component
class KodeverkClient(private val kodeRestTemplate: RestTemplate,
                     @Value("\${NAIS_APP_NAME}") private val appName: String) {

    private val logger = LoggerFactory.getLogger(KodeverkClient::class.java)

    private fun doRequest(builder: UriComponents) : String {
        try {
            val headers = HttpHeaders()
            headers["Nav-Consumer-Id"] = appName
            headers["Nav-Call-Id"] = UUID.randomUUID().toString()
            val requestEntity = HttpEntity<String>(headers)

            logger.debug("Header: $requestEntity, path: $builder")

            val response = kodeRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    String::class.java)

            return response.body ?: throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Feil ved konvetering av jsondata fra kodeverk")

        } catch (ce: HttpClientErrorException) {
            logger.error(ce.message, ce)
            throw ResponseStatusException(ce.statusCode, ce.message!!)
        } catch (se: HttpServerErrorException) {
            logger.error(se.message, se)
            throw ResponseStatusException(se.statusCode, se.message!!)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message!!)
        }
    }

    /**
     *  https://kodeverk.nais.adeo.no/api/v1/hierarki/LandkoderSammensattISO2/noder
     */
    fun hentHierarki(hierarki: String) : String {
        val path = "/api/v1/hierarki/{hierarki}/noder"

        val uriParams = mapOf("hierarki" to hierarki)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return doRequest(builder)
    }
}
