package no.nav.eessi.pensjon.models

import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.SakType
import java.time.LocalDate

data class IdentifisertPerson(
    val aktoerId: String,                               //fra PDL
    val personNavn: String?,                            //fra PDL
    val landkode: String?,         //fra PDL
    val geografiskTilknytning: String?,                              //fra PDL
    val personRelasjon: SEDPersonRelasjon,                 //fra PDL
    val fodselsdato: String? = null,              //innhenting fra FnrHelper og SED
    var personListe: List<IdentifisertPerson>? = null   //fra PDL){}
) {
    override fun toString(): String {
        return "IdentifisertPerson(aktoerId='$aktoerId', personNavn=$personNavn, landkode=$landkode, geografiskTilknytning=$geografiskTilknytning, personRelasjon=$personRelasjon)"
    }
    fun flereEnnEnPerson() = personListe != null && personListe!!.size > 1
}

data class SEDPersonRelasjon(
    val fnr: String?,
    val relasjon: Relasjon,
    val saktype: SakType? = null,
    val sedType: SedType? = null,
    val fdato: LocalDate? = null,
    val rinaDocumentId: String
)

enum class Relasjon {
    FORSIKRET,
    GJENLEVENDE,
    AVDOD,
    ANNET,
    BARN,
    FORSORGER
}
