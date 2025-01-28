package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
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
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        BreachNotice(
          crn = "X00000B",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    var breachNotice: MutableList<BreachNoticeEntity> = breachNoticeRepository.findByCrn("X00000B")
    assertThat(breachNotice).hasSize(1)
    assertThat(breachNotice.first().crn).isEqualTo("X00000B")
    assertThat(breachNotice.first().id).isNotNull()
  }

  @Test
  fun `should update a breach notice`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        BreachNotice(
          crn = "X00001B",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    var breachNotice: MutableList<BreachNoticeEntity> = breachNoticeRepository.findByCrn("X00001B")
    assertThat(breachNotice).hasSize(1)
    assertThat(breachNotice.first().crn).isEqualTo("X00001B")

    webTestClient.put()
      .uri("/breach-notice/"+breachNotice.first().id)
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        /* body = */
        BreachNotice(
          crn = "X00001B",
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
          nextAppointmentContact = null,
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
      .isOk

    var updatedBreachNotice: MutableList<BreachNoticeEntity> = breachNoticeRepository.findByCrn("X00001B")
    assertThat(updatedBreachNotice).hasSize(1)
    assertThat(updatedBreachNotice.first().crn).isEqualTo("X00001B")
    assertThat(updatedBreachNotice.first().nextAppointmentLocation).isEqualTo("NXT_LOCATION")
    assertThat(updatedBreachNotice.first().responsibleOfficer).isEqualTo("John Doe")
    assertThat(updatedBreachNotice.first().basicDetailsSaved).isEqualTo(true)
  }

  @Test
  fun `should fail to create if the crn is too long`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
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
  fun `should fail to create if the crn is too short`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(
        BreachNotice(
          crn = "X",
        ),
      )
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
