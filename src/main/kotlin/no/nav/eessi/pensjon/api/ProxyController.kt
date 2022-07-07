package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.KodeverkKlient
import no.nav.eessi.pensjon.klienter.Norg2Klient
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
@Protected
class ProxyController(private val norg2Klient: Norg2Klient,
                      private val kodeverkKient: KodeverkKlient,
                      @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

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
            norg2Klient.hentArbeidsfordelingEnheter(req)
        }
    }

    @GetMapping("/api/v1/hierarki/{hierarki}/noder")
    private fun kentLandkoderFraKodeverk(@PathVariable("hierarki", required = true) hierarki: String) : String {
        return proxyKodeverk.measure {
            kodeverkKient.hentHierarki(hierarki)
        }
    }

}