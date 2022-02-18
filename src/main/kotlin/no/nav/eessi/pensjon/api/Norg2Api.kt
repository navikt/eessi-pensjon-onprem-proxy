package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.norg2.Norg2Service
import no.nav.eessi.pensjon.klienter.norg2.NorgKlientRequest
import no.nav.eessi.pensjon.kodeverk.KodeverkClient
import no.nav.eessi.pensjon.models.Enhet
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.*

@RestController
@Unprotected
class ProxyController(private val norg2Service: Norg2Service,
                      private val kodeverkClient: KodeverkClient) {

    @PostMapping("/api/v1/arbeidsfordeling")
    fun norgArbeidsfordeling(@RequestBody req : NorgKlientRequest): Enhet? {
        return norg2Service.hentArbeidsfordelingEnhet(req)
    }


    @GetMapping("/api/v1/hierarki/{hierarki}/noder")
    private fun kentLandkoderFraKodeverk(@PathVariable("hierarki", required = true) hierarki: String) : String {
        return kodeverkClient.hentHierarki(hierarki)
    }

}