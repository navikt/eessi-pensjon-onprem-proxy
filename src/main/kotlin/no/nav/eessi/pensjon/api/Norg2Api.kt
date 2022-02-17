package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.norg2.Norg2Service
import no.nav.eessi.pensjon.klienter.norg2.NorgKlientRequest
import no.nav.eessi.pensjon.kodeverk.KodeverkClient
import no.nav.eessi.pensjon.kodeverk.Landkode
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

    @GetMapping("/hentLandKoder")
    fun hentLandKoder(): List<Landkode>? {
        return kodeverkClient.hentLandKoder()
    }

    @GetMapping("/finnLandkode")
    fun finnLandkode(@PathVariable("landkode", required = true) landkode: String): String? {
        return kodeverkClient.finnLandkode(landkode)
    }

    @GetMapping("/hentAlleLandkoder")
    fun hentAlleLandkoder(): String? {
        return kodeverkClient.hentAlleLandkoder()
    }

    @GetMapping("/hentLandkoderAlpha2")
    fun hentLandkoderAlpha2(): List<String>? {
        return kodeverkClient.hentLandkoderAlpha2()
    }
}