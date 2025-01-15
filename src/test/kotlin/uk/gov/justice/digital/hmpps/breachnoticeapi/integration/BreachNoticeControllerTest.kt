package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import java.time.LocalDate
import java.time.LocalDateTime

class BreachNoticeControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var objectMapper: ObjectMapper



    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/breach-notice")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue( BreachNotice(
        crn = "X03489B",
      dateOfLetter = LocalDate.now(),
      referenceNumber = "ABC1234565",
      responseRequiredDate = LocalDate.now(),
      breachNoticeTypeCode = "TYPE0NOGATIVE",
      breachConditionTypeCode = "TYPE0NOGATIVE",
      responsibleOfficer = "RESPONSIBLEPETE",
      contactNumber = "01912525252",
      nextAppointmentType = "TEST",
      nextAppointmentDate = LocalDateTime.now(),
      nextAppointmentLocation = "TEST_LOCATION",
      nextAppointmentOfficer = "TEST_OFFICER",
//      nextAppointmentContactId = null,
      completedDate = LocalDateTime.now(),

      )
        )
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

 }