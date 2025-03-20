package no.nav.eessi.pensjon.klienter

//import org.slf4j.LoggerFactory
//import org.springframework.http.HttpEntity
//import org.springframework.http.HttpHeaders
//import org.springframework.http.HttpStatus
//import org.springframework.http.MediaType
//import org.springframework.stereotype.Component
//import org.springframework.web.client.HttpStatusCodeException
//import org.springframework.web.client.RestTemplate
//import org.springframework.web.server.ResponseStatusException

//@Component
//class BehandleHendelseKlient(
//    private val behandleHendelseRestTemplate: RestTemplate) {
//
//    private val logger = LoggerFactory.getLogger(BehandleHendelseKlient::class.java)
//
//    fun opprettBehandleHendelse(utlandsHendelsemodel: String) {
//        try {
//            val headers = HttpHeaders()
//            headers.contentType = MediaType.APPLICATION_JSON
//            val httpEntity = HttpEntity(utlandsHendelsemodel, headers)
//            logger.debug("*** legger følgende melding på behandlehendlse tjenesten: $utlandsHendelsemodel ***")
//
//            behandleHendelseRestTemplate.postForEntity(
//                "/",
//                httpEntity,
//                String::class.java
//            )
//        } catch (ex: HttpStatusCodeException) {
//            logger.error("En feil oppstod under opprettelse av behandlehendlse ex: ", ex)
//            throw ResponseStatusException(ex.statusCode, "En feil oppstod under opprettelse av behandlehendelse ex: ${ex.message} body: ${ex.responseBodyAsString}")
//        } catch (ex: Exception) {
//            logger.error("En feil oppstod under opprettelse av behandlehendlse ex: ", ex)
//            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"En feil oppstod under opprettelse av behandlehendelse ex: ${ex.message}")
//        }
//
//        logger.debug("*** Ferdig med melding ***")
//    }
//}