package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDate
import java.time.LocalDateTime

class BreachNoticeCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Test
  fun `should create a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00000B",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00000B").single()
    assertThat(breachNotice.crn).isEqualTo("X00000B")
    assertThat(breachNotice.id).isNotNull()
  }

  @Test
  fun `should update a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00001C",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00001C").single()
    assertThat(breachNotice.crn).isEqualTo("X00001C")

    val breachNoticeBody = BreachNotice(
      crn = "X00001C",
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
      .bodyValue(
        breachNoticeBody,
      )
      .exchange()
      .expectStatus()
      .isOk

    val updatedBreachNotice = breachNoticeRepository.findByCrn("X00001C").single()
    assertThat(updatedBreachNotice.crn).isEqualTo("X00001C")
    assertThat(updatedBreachNotice.nextAppointmentLocation).isEqualTo("NXT_LOCATION")
    assertThat(updatedBreachNotice.responsibleOfficer).isEqualTo("John Doe")
    assertThat(updatedBreachNotice.basicDetailsSaved).isEqualTo(true)
  }

  @Test
  fun `should fail to create if the crn is too long`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00000B123456789123456",
        ),
      )
      .exchange()
      .expectStatus()
      .is5xxServerError
  }

  @Test
  fun `update should return server error if invalid format uuid passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00001G",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00001G")
    assertThat(breachNotice.first().crn).isEqualTo("X00001G")

    webTestClient.put()
      .uri("/breach-notice/" + "testone")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        /* body = */
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
      .expectStatus()
      .is5xxServerError
  }

  @Test
  fun `should delete a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00001D",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00001D")
    assertThat(breachNotice.first().crn).isEqualTo("X00001D")
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
      .bodyValue(
        BreachNotice(
          crn = "X00002D",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00002D")
    assertThat(breachNotice.first().crn).isEqualTo("X00002D")
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
  fun `delete should return server error if invalid format uuid passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X00003D",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X00003D")
    assertThat(breachNotice.first().crn).isEqualTo("X00003D")

    webTestClient.delete()
      .uri("/breach-notice/" + "TESTONE")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
