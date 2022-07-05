package no.nav.eessi.pensjon.integrasjon

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.eessi.pensjon.UnsecuredWebMvcTestLauncher
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = [UnsecuredWebMvcTestLauncher::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["unsecured-webmvctest"])
@AutoConfigureMockMvc
class PensjonIntegrasjonTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean(name =  "pensjonInformasjonRestTemplate")
    private lateinit var mockTemplate: RestTemplate

    @MockkBean(name = "personMottakRestTemplate")
    private lateinit var personMottakRestTemplate: RestTemplate

    @MockkBean(name = "fagmodulOAuth2RestTemplate")
    private lateinit var fagmodulOAuth2RestTemplate: RestTemplate

    @MockkBean(name = "behandleHendelseRestTemplate")
    private lateinit var behandleHendelseRestTemplate: RestTemplate

    @MockkBean(name = "bestemSakOidcRestTemplate")
    private lateinit var bestemSakOidcRestTemplate: RestTemplate

    @MockkBean(name = "norg2OidcRestTemplate")
    private lateinit var norg2OidcRestTemplate: RestTemplate

    @MockkBean(name = "kodeRestTemplate")
    private lateinit var kodeRestTemplate: RestTemplate

    @MockkBean
    private lateinit var clientConfigurationProperties: ClientConfigurationProperties

    @MockkBean
    private lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @MockkBean
    private lateinit var stsService: STSService

    @Test
    fun hentPensjonPaaVedtak() {
        val mockXMLResult = javaClass.getResource("/pensjon/Vedtak-AlderUtland.xml")!!.readText()
        val vedtakid = "59965174"
        val penreq = "<!xml>!sdfsdf!"

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, "application/xml")

        val responseEntity = ResponseEntity(mockXMLResult, headers, HttpStatus.OK)

        every { mockTemplate.exchange(eq("/vedtak/$vedtakid"), any(), any<HttpEntity<Unit>>(), eq(String::class.java)) } returns responseEntity

        val result = mockMvc.perform(
        MockMvcRequestBuilders.post("/pen/api/pensjonsinformasjon/v1/vedtak/$vedtakid")
        .contentType(MediaType.APPLICATION_JSON)
        .content(penreq))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val response = result.response.getContentAsString(charset("UTF-8"))

        assertEquals(mockXMLResult, response)


    }

}