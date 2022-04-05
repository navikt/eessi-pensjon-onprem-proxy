package no.nav.eessi.pensjon.api

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.pen.BehandleHendelseKlient
import no.nav.eessi.pensjon.pen.BestemSakKlient
import no.nav.eessi.pensjon.pen.BestemSakRequest
import no.nav.eessi.pensjon.pen.BestemSakResponse
import no.nav.eessi.pensjon.pen.PensjonsinformasjonClient
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
@Protected
class PensjonApi(private val pensjonsinformasjonClient: PensjonsinformasjonClient,
                 private val behandleHendelseKlient: BehandleHendelseKlient,
                 private val bestemSakKlient: BestemSakKlient,
                 @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PensjonApi::class.java) }

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
//        return proxyPensjonSak.measure {
            pensjonsinformasjonClient.hentAltPaaAktoerId(aktorid, req)
//        }
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/fnr")
    fun hentSakerPaaFnr(@RequestBody req : String, @RequestHeader ("fnr") fnr: String ): String {
        //return proxyPensjonSak.measure {
            logger.info("Pensjoninformasjonsrequest: $req")

            val penresult = pensjonsinformasjonClient.hentAltPaaFnr(fnr, req)
            logger.debug("pensjoninforesultat: $penresult")
            penresult
        //}
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/vedtak/{vedtakid}")
    fun hentVedtak(@RequestBody req : String, @PathVariable("vedtakid", required = true) vedtakid: String): String {
        //return proxyPensjonVedtak.measure {
            logger.info("hentvedtakreq: $req")
            val vedtaksresult = pensjonsinformasjonClient.hentAltPaaVedtak(vedtakid, req)
            if (vedtakid == "59965174") {
                logger.info("pensjonsinformasjon: $vedtaksresult")
            }
            vedtaksresult
        //}
    }

    @PostMapping("/pen/api/behandlehendelse/utland/v1/")
    fun behandleHendelse(@RequestBody req: String) {
        //proxyPensjonBehandleHendelse.measure {
            behandleHendelseKlient.opprettBehandleHendelse(req)
        //}
    }

    @PostMapping("/pen/api/bestemsak/v1")
    fun bestemSak(@RequestBody req: BestemSakRequest): BestemSakResponse? {
        //return proxyBestemsak.measure {
            bestemSakKlient.kallBestemSak(req)
        //}
    }

}