package no.nav.eessi.pensjon.api

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.pen.*
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

@RestController
@Protected
class PensjonApi(private val pensjonsinformasjonClient: PensjonsinformasjonClient,
                 private val behandleHendelseKlient: BehandleHendelseKlient,
                 private val bestemSakKlient: BestemSakKlient,
                 @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private lateinit var proxyBestemsak: MetricsHelper.Metric
    private lateinit var proxyPensjonSak: MetricsHelper.Metric
    private lateinit var proxyPensjonVedtak: MetricsHelper.Metric
    private lateinit var proxyPensjonBehandleHendelse: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        proxyBestemsak = metricsHelper.init("proxyBestemsak")
        proxyBestemsak = metricsHelper.init("proxyPensjonSak")
        proxyBestemsak = metricsHelper.init("proxyPensjonVedtak")
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/aktor/{aktorid}")
    fun hentSaker(@RequestBody req : String, @PathVariable("aktorid", required = true) aktorid: String): String {
        return proxyPensjonSak.measure {
            pensjonsinformasjonClient.hentAltPaaAktoerId(aktorid, req)
        }
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/vedtak/{vedtakid}")
    fun hentVedtak(@RequestBody req : String, @PathVariable("vedtakid", required = true) vedtakid: String): String {
        return proxyPensjonVedtak.measure {
            pensjonsinformasjonClient.hentAltPaaVedtak(vedtakid, req)
        }
    }

    @PostMapping("/pen/api/behandlehendelse/utland/v1/")
    fun behandleHendelse(@RequestBody req: String) {
        proxyPensjonBehandleHendelse.measure {
            behandleHendelseKlient.opprettBehandleHendelse(req)
        }
    }

    @PostMapping("/pen/api/bestemsak/v1")
    fun bestemSak(@RequestBody req: BestemSakRequest): BestemSakResponse? {
        return proxyBestemsak.measure {
            bestemSakKlient.kallBestemSak(req)
        }
    }

}