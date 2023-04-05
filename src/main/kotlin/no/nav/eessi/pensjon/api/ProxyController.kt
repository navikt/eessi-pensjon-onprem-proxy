package no.nav.eessi.pensjon.api

import jakarta.annotation.PostConstruct
import no.nav.eessi.pensjon.klienter.Norg2Klient
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@Protected
class ProxyController(private val norg2Klient: Norg2Klient,
                      @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

    private lateinit var proxyNorg2: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        proxyNorg2 = metricsHelper.init("proxyNorg2")
    }

    @PostMapping("/api/v1/arbeidsfordeling")
    fun norgArbeidsfordeling(@RequestBody req : String): String? {
        return proxyNorg2.measure {
            norg2Klient.hentArbeidsfordelingEnheter(req)
        }
    }

}