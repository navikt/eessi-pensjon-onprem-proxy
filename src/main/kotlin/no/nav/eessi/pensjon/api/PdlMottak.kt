package no.nav.eessi.pensjon.api

import no.nav.eessi.pensjon.pdlmottak.PersonMottakKlient
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class PdlMottak(private val personMottak: PersonMottakKlient) {

    @PostMapping("/api/v1/endringer")
    fun hentSaker(@RequestBody personopplysningRequest : String, @RequestHeader ("Nav-Call-Id") navId: String ): Boolean {
     return personMottak.opprettPersonopplysning(personopplysningRequest, navId)
    }

}