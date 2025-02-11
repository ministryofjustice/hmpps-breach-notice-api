package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class PdfGenerationTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Test
  fun `get PDF should return a 200 response`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        BreachNotice(
          crn = "X00002A",
          referenceNumber = "BRE-000001-A"
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00002A")
    assertThat(breachNotice.first().crn).isEqualTo("X00002A")

    webTestClient.get()
      .uri("/breach-notice/" + breachNotice[0].id + "/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader()
      .contentType(MediaType.APPLICATION_PDF)
      .expectHeader()
      .contentDisposition(ContentDisposition.attachment().filename("Breach_Notice_" + breachNotice[0].crn + "_" + breachNotice[0].referenceNumber + ".pdf").build())
  }

  @Test
  fun `get PDF should return a 404 response if breach not found`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        BreachNotice(
          crn = "X00002A",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00002A")
    assertThat(breachNotice.first().crn).isEqualTo("X00002A")

    webTestClient.get()
      .uri("/breach-notice/"+ UUID.randomUUID() +"/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
