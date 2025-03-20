package no.nav.eessi.pensjon.klienter

import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

//@Component
//class FagmodulKlient(private val fagmodulOAuth2RestTemplate: RestTemplate) {
//
//    private val logger = LoggerFactory.getLogger(FagmodulKlient::class.java)
//
//    fun hentJsonDataFraFagmodul(requestPath: String): String {
//        return try {
//            logger.info("Kaller fagmodul med: $requestPath")
//            val responseEntity = fagmodulOAuth2RestTemplate.exchange(
//                requestPath,
//                HttpMethod.GET,
//                null,
//                String::class.java
//            )
//            responseEntity.body!!
//        } catch (ex: Exception) {
//            throw ex
//        }
//    }
//}
//
