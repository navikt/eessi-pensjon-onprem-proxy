package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.services.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.services.ldap.BrukerInformasjonService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class BrukerinformasjonController(private val ldapService: BrukerInformasjonService) {

    @GetMapping("/brukerinfo/{navident}")
    fun hentBrukerInformasjon(@PathVariable("navident", required = true) navident: String): BrukerInformasjon {
        return ldapService.hentBrukerInformasjon(navident)
    }


}