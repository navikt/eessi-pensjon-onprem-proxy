package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@Protected
@RestController
class BrukerinformasjonController(
    private val ldapService: BrukerInformasjonService,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

    private lateinit var proxyLDAP: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
      proxyLDAP = metricsHelper.init("proxyLDAP")
    }

    @GetMapping("/brukerinfo/{navident}")
    fun hentBrukerInformasjon(@PathVariable("navident", required = true) navident: String): BrukerInformasjon {
        return proxyLDAP.measure {
            ldapService.hentBrukerInformasjon(navident)
        }
    }

}