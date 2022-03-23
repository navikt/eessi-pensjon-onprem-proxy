package no.nav.eessi.pensjon.api

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.klienter.norg2.Norg2Klient
import no.nav.eessi.pensjon.kodeverk.KodeverkClient
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.models.Enhet
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

@RestController
@Protected
class ProxyController(private val norg2Service: Norg2Klient,
                      private val kodeverkClient: KodeverkClient,
                      @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private lateinit var proxyNorg2: MetricsHelper.Metric
    private lateinit var proxyKodeverk: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        proxyNorg2 = metricsHelper.init("proxyNorg2")
        proxyKodeverk = metricsHelper.init("proxyKodeverk")
    }

    @PostMapping("/api/v1/arbeidsfordeling")
    fun norgArbeidsfordeling(@RequestBody req : String): String? {
        return proxyNorg2.measure {
            norg2Service.hentArbeidsfordelingEnheter(req)
        }
    }

    @GetMapping("/api/v1/hierarki/{hierarki}/noder")
    private fun kentLandkoderFraKodeverk(@PathVariable("hierarki", required = true) hierarki: String) : String {
        return proxyKodeverk.measure {
            kodeverkClient.hentHierarki(hierarki)
        }
    }

}