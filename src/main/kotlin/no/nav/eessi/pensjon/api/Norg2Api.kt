package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.klienter.norg2.Norg2Service
import no.nav.eessi.pensjon.klienter.norg2.NorgKlientRequest
import no.nav.eessi.pensjon.models.Enhet
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class Norg2Api(private val norg2Service: Norg2Service) {

    @PostMapping("/api/v1/arbeidsfordeling")
    fun norgArbeidsfordeling(@RequestBody req : NorgKlientRequest): Enhet? {
        return norg2Service.hentArbeidsfordelingEnhet(req)
    }
}