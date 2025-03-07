package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties =
  ["spring.autoconfigure.exclude=uk.gov.justice.hmpps.sqs.HmppsSqsConfiguration"],
)
class BreachNoticeCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Test
  fun `should create a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000001").single()
    assertThat(breachNotice.crn).isEqualTo("X000001")
    assertThat(breachNotice.id).isNotNull()
  }

  @Test
  fun `should update a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000002"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000002").single()
    assertThat(breachNotice.crn).isEqualTo("X000002")

    val breachNoticeBody = BreachNotice(
      crn = "X000002",
      breachConditionTypeCode = "TYPE_CODE",
      titleAndFullName = "Mr Joe Bloggs",
      dateOfLetter = LocalDate.now(),
      referenceNumber = "REFERENCE_NUMBER",
      responseRequiredDate = LocalDate.now(),
      breachNoticeTypeCode = "BRCH",
      responsibleOfficer = "John Doe",
      contactNumber = "01912525252",
      nextAppointmentType = "NXTTYP",
      nextAppointmentDate = LocalDateTime.now(),
      nextAppointmentLocation = "NXT_LOCATION",
      nextAppointmentOfficer = "APPT_OFFICER",
      nextAppointmentId = null,
      completedDate = LocalDateTime.now(),
      offenderAddress = Address(
        addressId = 25,
        type = "ENDO",
        buildingName = "MOO",
      ),
      replyAddress = null,
      basicDetailsSaved = true,
    )

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice.id)
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(breachNoticeBody)
      .exchange()
      .expectStatus()
      .isOk

    val updatedBreachNotice = breachNoticeRepository.findByCrn("X000002").single()
    assertThat(updatedBreachNotice.crn).isEqualTo("X000002")
    assertThat(updatedBreachNotice.nextAppointmentLocation).isEqualTo("NXT_LOCATION")
    assertThat(updatedBreachNotice.responsibleOfficer).isEqualTo("John Doe")
    assertThat(updatedBreachNotice.basicDetailsSaved).isEqualTo(true)
  }

  @Test
  fun `should fail to create if the crn is too long`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000001123456789123456"))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.userMessage").isEqualTo("""Field: crn - must match "^[A-Z][0-9]{6}"""")
  }

  @Test
  fun `update should return bad request if invalid format uuid passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000003"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000003")
    assertThat(breachNotice.first().crn).isEqualTo("X000003")

    webTestClient.put()
      .uri("/breach-notice/" + "testone")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00001Z",
          breachConditionTypeCode = "TYPE_CODE",
          titleAndFullName = "Mr Joe Bloggs",
          dateOfLetter = LocalDate.now(),
          referenceNumber = "REFERENCE_NUMBER",
          responseRequiredDate = LocalDate.now(),
          breachNoticeTypeCode = "BRCH",
          responsibleOfficer = "John Doe",
          contactNumber = "01912525252",
          nextAppointmentType = "NXTTYP",
          nextAppointmentDate = LocalDateTime.now(),
          nextAppointmentLocation = "NXT_LOCATION",
          nextAppointmentOfficer = "APPT_OFFICER",
          nextAppointmentId = null,
          completedDate = LocalDateTime.now(),
          offenderAddress = Address(
            addressId = 25,
            type = "ENDO",
            buildingName = "MOO",
          ),
          replyAddress = null,
          basicDetailsSaved = true,
        ),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.userMessage").value(containsString("Invalid UUID string: testone"))
  }

  @Test
  fun `should delete a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000004"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000004")
    assertThat(breachNotice.first().crn).isEqualTo("X000004")
    assertThat(breachNotice.first().id).isNotNull()

    webTestClient.delete()
      .uri("/breach-notice/" + breachNotice.first().id)
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    val purgedBreachNotice = breachNoticeRepository.findById(breachNotice.first().id)
    assertThat(purgedBreachNotice.isEmpty)
  }

  @Test
  fun `error on delete a breach notice when no matching uuid`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000005"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000005")
    assertThat(breachNotice.first().crn).isEqualTo("X000005")
    assertThat(breachNotice.first().id).isNotNull()

    // Non-existent uuid
    webTestClient.delete()
      .uri("/breach-notice/" + "00000000-0000-4000-8000-000000000000")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isNotFound

    var refreshedBreachNotice = breachNoticeRepository.findById(breachNotice.first().id)
    assertThat(refreshedBreachNotice.isPresent)

    // Existing, now-deleted uuid
    webTestClient.delete()
      .uri("/breach-notice/" + breachNotice.first().id)
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    refreshedBreachNotice = breachNoticeRepository.findById(breachNotice.first().id)
    assertThat(refreshedBreachNotice.isEmpty)

    webTestClient.delete()
      .uri("/breach-notice/" + breachNotice.first().id)
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `delete should return bad request if invalid format uuid passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000006"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000006")
    assertThat(breachNotice.first().crn).isEqualTo("X000006")

    webTestClient.delete()
      .uri("/breach-notice/" + "TESTONE")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.userMessage").value(containsString("Invalid UUID string: TESTONE"))
  }
}
