package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.norg2.Norg2Klient
import no.nav.eessi.pensjon.kodeverk.KodeverkClient
import no.nav.eessi.pensjon.models.Enhet
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.*

@RestController
@Protected
class ProxyController(private val norg2Service: Norg2Klient,
                      private val kodeverkClient: KodeverkClient) {

    @PostMapping("/api/v1/arbeidsfordeling")
    fun norgArbeidsfordeling(@RequestBody req : String): String? {
        return norg2Service.hentArbeidsfordelingEnheter(req)
    }

    @GetMapping("/api/v1/hierarki/{hierarki}/noder")
    private fun kentLandkoderFraKodeverk(@PathVariable("hierarki", required = true) hierarki: String) : String {
        return kodeverkClient.hentHierarki(hierarki)
    }

}