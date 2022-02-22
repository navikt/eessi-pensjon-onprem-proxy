package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.pen.PensjonsinformasjonClient
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class PensjonApi(private val pensjonsinformasjonClient: PensjonsinformasjonClient) {

    @PostMapping("/pen/api/pensjonsinformasjon/v1/aktor/{aktorid}")
    fun hentSaker(@RequestBody req : String, @PathVariable("aktorid", required = true) aktorid: String): String {
        //https://pensjon-pen-q2.nais.pr≈aktor/2790267313355
      return pensjonsinformasjonClient.hentAltPaaAktoerId(aktorid, req)
    }

    @PostMapping("/pen/api/pensjonsinformasjon/v1/vedtak/{vedtakid}")
    fun hentVedtak(@RequestBody req : String, @PathVariable("vedtakid", required = true) vedtakid: String): String {
        return pensjonsinformasjonClient.hentAltPaaVedtak(vedtakid, req)
    }

}