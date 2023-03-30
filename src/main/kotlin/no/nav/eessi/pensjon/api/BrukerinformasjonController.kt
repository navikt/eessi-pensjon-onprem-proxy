package no.nav.eessi.pensjon.api

import jakarta.annotation.PostConstruct
import no.nav.eessi.pensjon.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class BrukerinformasjonController(
    private val ldapService: BrukerInformasjonService,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {
    private val logger: Logger by lazy { LoggerFactory.getLogger(BrukerinformasjonController::class.java) }

    private lateinit var proxyLDAP: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
      proxyLDAP = metricsHelper.init("proxyLDAP")
    }

    @GetMapping("/brukerinfo/{navident}")
    fun hentBrukerInformasjon(@PathVariable("navident", required = true) navident: String): BrukerInformasjon {
        logger.info("Henter brukerinformasjon for Ident: $navident")
        return proxyLDAP.measure {
            ldapService.hentBrukerInformasjon(navident)
        }
    }

}