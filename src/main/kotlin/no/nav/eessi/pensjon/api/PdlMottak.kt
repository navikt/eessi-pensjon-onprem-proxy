package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.PersonMottakKlient
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
@Protected
class PdlMottak(private val personMottak: PersonMottakKlient,
                @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

    private lateinit var proxyPdlMottak: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        proxyPdlMottak = metricsHelper.init("proxyPdlMottak")
    }


    @PostMapping("/api/v1/endringer")
    fun hentSaker(@RequestBody personopplysningRequest : String, @RequestHeader ("Nav-Call-Id") navId: String ): Boolean {
        return proxyPdlMottak.measure {
            personMottak.opprettPersonopplysning(personopplysningRequest, navId)
        }
    }

}