package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.pen.*
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.*

@RestController
@Protected
class PensjonApi(private val pensjonsinformasjonClient: PensjonsinformasjonClient,
                 private val behandleHendelseKlient: BehandleHendelseKlient,
                 private val bestemSakKlient: BestemSakKlient) {

    @PostMapping("/pen/api/pensjonsinformasjon/v1/aktor/{aktorid}")
    fun hentSaker(@RequestBody req : String, @PathVariable("aktorid", required = true) aktorid: String): String {
        //https://pensjon-pen-q2.nais.pr≈aktor/2790267313355
      return pensjonsinformasjonClient.hentAltPaaAktoerId(aktorid, req)
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/vedtak/{vedtakid}")
    fun hentVedtak(@RequestBody req : String, @PathVariable("vedtakid", required = true) vedtakid: String): String {
        return pensjonsinformasjonClient.hentAltPaaVedtak(vedtakid, req)
    }

    @PostMapping("/pen/api/behandlehendelse/utland/v1/")
    fun behandleHendelse(@RequestBody req: String) {
       behandleHendelseKlient.opprettBehandleHendelse(req)
    }

    @GetMapping("/pen/api/bestemsak/v1")
    fun bestemSak(@RequestBody req: BestemSakRequest): BestemSakResponse? {
        return bestemSakKlient.kallBestemSak(req)
    }
}