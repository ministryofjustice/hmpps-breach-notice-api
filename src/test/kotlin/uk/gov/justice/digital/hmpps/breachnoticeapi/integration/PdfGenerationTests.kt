package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.util.UUID

class PdfGenerationTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Test
  fun `get PDF should return a 200 response`() {
    stubGeneratePdf()

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000007",
          referenceNumber = "BRE-000001-A",
        ),
      )
      .exchange()
      .expectStatus().isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000007")
    assertThat(breachNotice.first().crn).isEqualTo("X000007")

    webTestClient.get()
      .uri("/breach-notice/" + breachNotice[0].id + "/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
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
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000007"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000007")
    assertThat(breachNotice.first().crn).isEqualTo("X000007")

    webTestClient.get()
      .uri("/breach-notice/" + UUID.randomUUID() + "/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus().isNotFound
  }
}
