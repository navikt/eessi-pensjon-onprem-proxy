package no.nav.eessi.pensjon.klienter


import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Service
class PensjonsinformasjonClient(private val pensjonInformasjonRestTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(PensjonsinformasjonClient::class.java)

    private enum class REQUESTPATH(val path: String) {
        FNR("/fnr"),
        VEDTAK("/vedtak"),
        AKTOR("/aktor");
    }

    @Deprecated("Replace with hentAltPaaFNR")
    fun hentAltPaaAktoerId(aktoerId: String, requestBody: String): String {
        return doRequest(REQUESTPATH.AKTOR, aktoerId, requestBody)
    }

    fun hentAltPaaFnr(fnr: String, requestBody: String): String {
        return doRequest(REQUESTPATH.FNR, fnr, requestBody)

    }

    fun hentAltPaaVedtak(vedtaksId: String, requestBody: String): String {
        return doRequest(REQUESTPATH.VEDTAK, vedtaksId, requestBody)
    }

    private fun doRequest(path: REQUESTPATH, id: String, requestBody: String): String {

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)

        if (path == REQUESTPATH.FNR) {
            headers.add("fnr", id)
        }

        val requestEntity = HttpEntity(requestBody, headers)

        val uriBuilder = if (path == REQUESTPATH.FNR) {
            UriComponentsBuilder.fromPath(path.path)
        } else {
            UriComponentsBuilder.fromPath(path.path).pathSegment(id)
        }
        logger.info("Pensjoninformasjon Uri: ${uriBuilder.toUriString()}")

            return try {
                val responseEntity = pensjonInformasjonRestTemplate.exchange(
                        uriBuilder.toUriString(),
                        HttpMethod.POST,
                        requestEntity,
                        String::class.java)
                logger.debug("*** body: ${responseEntity.body!!}")
                responseEntity.body!!

            } catch (hsee: HttpServerErrorException) {
                val errorBody = hsee.responseBodyAsString
                logger.error("PensjoninformasjonService feiler med HttpServerError body: $errorBody", hsee)
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PensjoninformasjonService feiler med innhenting av pensjoninformasjon fra PESYS, prøv igjen om litt")
            } catch (hcee: HttpClientErrorException) {
                val errorBody = hcee.responseBodyAsString
                logger.error("PensjoninformasjonService feiler med HttpClientError body: $errorBody", hcee)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "PensjoninformasjonService feiler med innhenting av pensjoninformasjon fra PESYS, prøv igjen om litt")
            } catch (ex: Exception) {
                logger.error("PensjoninformasjonService feiler med kontakt til PESYS pensjoninformajson, ${ex.message}", ex)
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PensjoninformasjonService feiler med ukjent feil mot PESYS. melding: ${ex.message}")
            }

    }

}

