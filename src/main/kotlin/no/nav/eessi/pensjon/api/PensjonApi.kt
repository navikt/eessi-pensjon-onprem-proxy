package no.nav.eessi.pensjon.api

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.annotation.PostConstruct
import no.nav.eessi.pensjon.klienter.BehandleHendelseKlient
import no.nav.eessi.pensjon.klienter.BestemSakKlient
import no.nav.eessi.pensjon.klienter.BestemSakRequest
import no.nav.eessi.pensjon.klienter.BestemSakResponse
import no.nav.eessi.pensjon.klienter.FagmodulKlient
import no.nav.eessi.pensjon.klienter.PensjonsinformasjonClient
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
//@Protected
class PensjonApi(private val pensjonsinformasjonClient: PensjonsinformasjonClient,
                 private val behandleHendelseKlient: BehandleHendelseKlient,
                 private val bestemSakKlient: BestemSakKlient,
                 private val fagmodulKlient: FagmodulKlient,
                 @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PensjonApi::class.java) }

    private lateinit var proxyBestemsak: MetricsHelper.Metric
    private lateinit var proxyPensjonSak: MetricsHelper.Metric
    private lateinit var proxyPensjonSakFnr: MetricsHelper.Metric
    private lateinit var proxyPensjonVedtak: MetricsHelper.Metric
    private lateinit var proxyPensjonBehandleHendelse: MetricsHelper.Metric
    private lateinit var proxyPensjonUtland: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        proxyPensjonBehandleHendelse = metricsHelper.init("proxyHendelse")
        proxyBestemsak = metricsHelper.init("proxyBestemsak")
        proxyPensjonSak = metricsHelper.init("proxyPensjonSak")
        proxyPensjonSakFnr = metricsHelper.init("proxyPensjonSakFnr")
        proxyPensjonVedtak = metricsHelper.init("proxyPensjonVedtak")
        proxyPensjonUtland = metricsHelper.init("proxyPensjonUtland")
    }

    @Protected
    @PostMapping("/pen/api/pensjonsinformasjon/v1/aktor/{aktorid}")
    fun hentSaker(@RequestBody req : String, @PathVariable("aktorid", required = true) aktorid: String): String {
        return proxyPensjonSak.measure {
            @Suppress("DEPRECATION")
            pensjonsinformasjonClient.hentAltPaaAktoerId(aktorid, req)
        }
    }

    @Protected
    @PostMapping("/pen/api/pensjonsinformasjon/v1/fnr")
    fun hentSakerPaaFnr(@RequestBody req : String, @RequestHeader ("fnr") fnr: String ): String {
        return proxyPensjonSakFnr.measure {
            logger.debug("Pensjoninformasjonsrequest: $req")
            pensjonsinformasjonClient.hentAltPaaFnr(fnr, req)
        }
    }

    @Protected
    @PostMapping("/pen/api/pensjonsinformasjon/v1/vedtak/{vedtakid}")
    fun hentVedtak(@RequestBody req : String, @PathVariable("vedtakid", required = true) vedtakid: String): String {
        return proxyPensjonVedtak.measure {
            logger.debug("Pensjoninformasjonsrequest: $req")
            pensjonsinformasjonClient.hentAltPaaVedtak(vedtakid, req)
       }
    }

    @Protected
    @PostMapping("/pen/api/behandlehendelse/utland/v1/")
    fun behandleHendelse(@RequestBody req: String) {
        proxyPensjonBehandleHendelse.measure {
            behandleHendelseKlient.opprettBehandleHendelse(req)
        }
    }

    @Protected
    @PostMapping("/pen/api/bestemsak/v1")
    fun bestemSak(@RequestBody req: BestemSakRequest): BestemSakResponse? {
        return proxyBestemsak.measure {
            bestemSakKlient.kallBestemSak(req)
        }
    }

    @Unprotected
    @GetMapping("/pesys/hentKravUtland/{bucId}")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    fun hentKravUtland(@PathVariable("bucId", required = true) bucId: String, @RequestHeader(HttpHeaders.AUTHORIZATION) auth: String? = null): String {
        logger.debug("tokenauth: $auth")
        return proxyPensjonUtland.measure {
            fagmodulKlient.hentJsonDataFraFagmodul("/pesys/hentKravUtland/$bucId")
        }
    }

}