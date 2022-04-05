package no.nav.eessi.pensjon.api

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.models.SakInformasjon
import no.nav.eessi.pensjon.pen.BehandleHendelseKlient
import no.nav.eessi.pensjon.pen.BestemSakKlient
import no.nav.eessi.pensjon.pen.BestemSakRequest
import no.nav.eessi.pensjon.pen.BestemSakResponse
import no.nav.eessi.pensjon.pen.PensjonsinformasjonClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PensjonApiTest {


    private val pensjonsinformasjonClient: PensjonsinformasjonClient = mockk()
    private val behandleHendelseKlient: BehandleHendelseKlient = mockk()
    private val bestemSakKlient: BestemSakKlient = mockk()

    private val api = PensjonApi(pensjonsinformasjonClient, behandleHendelseKlient, bestemSakKlient)

    @BeforeEach
    fun before() {
        api.initMetrics()
    }

    @Test
    fun hentAltPaaAktoer() {
        val req = "<!xml>sasdad"
        val aktoer = "1231231231"
        val xmlString = javaClass.getResource("/pensjon/Vedtak-AlderUtland.xml")!!.readText()

        every { pensjonsinformasjonClient.hentAltPaaAktoerId(any(), any()) } returns xmlString

        val result = api.hentSaker(req, aktoer)

        verify (exactly = 1) { pensjonsinformasjonClient.hentAltPaaAktoerId(any(), any()) }
        assertEquals(xmlString, result)

    }


    @Test
    fun hentAltPaaFnr() {
        val req = "<!xml>sasdad"
        val fnr = "1231231231"
        val xmlString = javaClass.getResource("/pensjon/Vedtak-AlderUtland.xml")!!.readText()

        every { pensjonsinformasjonClient.hentAltPaaFnr(any(), any()) } returns xmlString

        val result = api.hentSakerPaaFnr(req, fnr)
        verify (exactly = 1) { pensjonsinformasjonClient.hentAltPaaFnr(any(), any()) }
        assertEquals(xmlString, result)

    }

    @Test
    fun hentAltVedtak() {
        val req = "<!xml>sasdad"
        val vedtak = "1231231231"
        val xmlString = javaClass.getResource("/pensjon/Vedtak-AlderUtland.xml")!!.readText()

        every { pensjonsinformasjonClient.hentAltPaaVedtak(any(), any()) } returns xmlString

        val result = api.hentVedtak (req, vedtak)
        verify (exactly = 1) { pensjonsinformasjonClient.hentAltPaaVedtak(any(), any()) }
        assertEquals(xmlString, result)

    }

    @Test
    fun hentBestamSak() {
        val bestemReq = mockk<BestemSakRequest>(relaxed = true)
        val mockResponse = BestemSakResponse(sakInformasjonListe = listOf(mockk<SakInformasjon>(relaxed = true)))

        every { bestemSakKlient.kallBestemSak(any()) } returns mockResponse

        val result = bestemSakKlient.kallBestemSak(bestemReq)

        verify (exactly = 1) { bestemSakKlient.kallBestemSak(bestemReq) }
        assertEquals(mockResponse, result)

    }

    @Test
    fun hendelse() {

        justRun {  behandleHendelseKlient.opprettBehandleHendelse(any()) }

        api.behandleHendelse("bla bla bla bla")

        verify (exactly = 1) { behandleHendelseKlient.opprettBehandleHendelse(any()) }

    }

}