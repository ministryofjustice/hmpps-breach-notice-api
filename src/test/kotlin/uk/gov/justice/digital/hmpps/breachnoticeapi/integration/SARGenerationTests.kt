package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SARGenerationTests : IntegrationTestBase() {

  @Test
  fun `should return 200 response on valid request`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000001", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000001").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should return 204 response on valid request with no content`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000002", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "Z000002").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isNoContent
  }

  @Test
  fun `should return 403 response on request without ROLE_SAR_DATA_ACCESS`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000003", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000003").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 401 response without a token`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000004", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000004").build() }
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return 209 response when no crn is passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000005", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("prn", "X000005").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isEqualTo(209)
      .expectBody(String::class.java)
      .isEqualTo("Supplied subject identifier is not recognised by this service")
  }

  @Test
  fun `should return 209 response when no parameters are passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000006", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isEqualTo(209)
      .expectBody(String::class.java)
      .isEqualTo("Supplied subject identifier is not recognised by this service")
  }

  @Test
  fun `should return 209 response when invalid crn format is passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000007", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "MyNewCrn").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isEqualTo(209)
      .expectBody(String::class.java)
      .isEqualTo("Supplied subject identifier is not recognised by this service")
  }

  @Test
  fun `should return 209 response when date format is incorrect`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000008", dateOfLetter = LocalDate.now()))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder ->
        builder.path("/subject-access-request").queryParam("crn", "X000008")
          .queryParam("fromDate", "2000-01-01-23:59:59")
          .queryParam("toDate", "31-Dec-2019").build()
      }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isEqualTo(209)
      .expectBody(String::class.java)
      .isEqualTo("Supplied date format is not recognised by this service")
  }

  @Test
  fun `should return correct information fields and clear personal details`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000009",
          titleAndFullName = "Mr Joe Bloggs",
          dateOfLetter = LocalDate.of(2010, 1, 1),
          referenceNumber = "REFERENCE_NUMBER",
          responseRequiredDate = LocalDate.of(2012, 1, 1),
          breachNoticeTypeCode = "BRCH",
          breachNoticeTypeDescription = "BREACH DESCRIPTION",
          breachConditionTypeCode = "TYPE_CODE",
          breachConditionTypeDescription = "CONDITION DESCRIPTION",
          breachSentenceTypeCode = "BR_SNTC",
          breachSentenceTypeDescription = "SENTENCE DESCRIPTION",
          responsibleOfficer = "John Doe",
          contactNumber = "01912525252",
          nextAppointmentType = "NXTTYP",
          nextAppointmentDate = LocalDateTime.of(2010, 12, 31, 10, 0),
          nextAppointmentLocation = "NXT_LOCATION",
          nextAppointmentOfficer = "APPT_OFFICER",
          nextAppointmentId = 1234,
          completedDate = LocalDateTime.of(2011, 1, 1, 15, 0),
          offenderAddress = Address(
            addressId = 25,
            buildingName = "MOO",
            addressNumber = "1",
            streetName = "strasse",
            district = "westminster",
            townCity = "London",
            county = "Metropolitan",
            postcode = "AB123CD",
          ),
          replyAddress = Address(
            addressId = 1,
            buildingName = "ADDR",
            addressNumber = "2",
            streetName = "A Street 1",
            district = "The fun district",
            townCity = "NoddyLand",
            county = "Suffolk",
            postcode = "ZY987XW",
          ),
          basicDetailsSaved = true,
          warningTypeSaved = true,
          warningDetailsSaved = false,
          nextAppointmentSaved = false,
          useDefaultAddress = true,
          useDefaultReplyAddress = true,
          optionalNumberChecked = false,
          optionalNumber = "01234567891",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000009").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(1)
      .jsonPath("$.[0].crn").value(containsString("X000009"))
      .jsonPath("$.[0].dateOfLetter").isEqualTo(listOf(2010, 1, 1))
      .jsonPath("$.[0].referenceNumber").value(containsString("REFERENCE_NUMBER"))
      .jsonPath("$.[0].breachNoticeTypeCode").value(containsString("BRCH"))
      .jsonPath("$.[0].breachNoticeTypeDescription").value(containsString("BREACH DESCRIPTION"))
      .jsonPath("$.[0].breachConditionTypeCode").value(containsString("TYPE_CODE"))
      .jsonPath("$.[0].breachConditionTypeDescription").value(containsString("CONDITION DESCRIPTION"))
      .jsonPath("$.[0].breachSentenceTypeCode").value(containsString("BR_SNTC"))
      .jsonPath("$.[0].breachSentenceTypeDescription").value(containsString("SENTENCE DESCRIPTION"))
      .jsonPath("$.[0].responseRequiredDate").isEqualTo(listOf(2012, 1, 1))
      .jsonPath("$.[0].nextAppointmentType").value(containsString("NXTTYP"))
      .jsonPath("$.[0].nextAppointmentDate").isEqualTo(listOf(2010, 12, 31, 10, 0))
      .jsonPath("$.[0].nextAppointmentLocation").value(containsString("NXT_LOCATION"))
      .jsonPath("$.[0].nextAppointmentId").isEqualTo(1234)
      .jsonPath("$.[0].completedDate").isEqualTo(listOf(2011, 1, 1, 15, 0))
      .jsonPath("$.[0].offenderAddress.addressId").isEqualTo(25)
      .jsonPath("$.[0].offenderAddress.buildingName").value(containsString("MOO"))
      .jsonPath("$.[0].offenderAddress.addressNumber").value(containsString("1"))
      .jsonPath("$.[0].offenderAddress.streetName").value(containsString("strasse"))
      .jsonPath("$.[0].offenderAddress.district").value(containsString("westminster"))
      .jsonPath("$.[0].offenderAddress.townCity").value(containsString("London"))
      .jsonPath("$.[0].offenderAddress.county").value(containsString("Metropolitan"))
      .jsonPath("$.[0].offenderAddress.postcode").value(containsString("AB123CD"))
      .jsonPath("$.[0].replyAddress.addressId").isEqualTo(1)
      .jsonPath("$.[0].replyAddress.buildingName").value(containsString("ADDR"))
      .jsonPath("$.[0].replyAddress.addressNumber").value(containsString("2"))
      .jsonPath("$.[0].replyAddress.streetName").value(containsString("A Street 1"))
      .jsonPath("$.[0].replyAddress.district").value(containsString("The fun district"))
      .jsonPath("$.[0].replyAddress.townCity").value(containsString("NoddyLand"))
      .jsonPath("$.[0].replyAddress.county").value(containsString("Suffolk"))
      .jsonPath("$.[0].replyAddress.postcode").value(containsString("ZY987XW"))
      .jsonPath("$.[0].basicDetailsSaved").isEqualTo(true)
      .jsonPath("$.[0].warningTypeSaved").isEqualTo(true)
      .jsonPath("$.[0].warningDetailsSaved").isEqualTo(false)
      .jsonPath("$.[0].nextAppointmentSaved").isEqualTo(false)
      .jsonPath("$.[0].useDefaultAddress").isEqualTo(true)
      .jsonPath("$.[0].useDefaultReplyAddress").isEqualTo(true)
      .jsonPath("$.[0].optionalNumberChecked").isEqualTo(false)
      // Cleared information
      .jsonPath("$.[0].titleAndFullName").isEmpty()
      .jsonPath("$.[0].responsibleOfficer").isEmpty()
      .jsonPath("$.[0].contactNumber").isEmpty()
      .jsonPath("$.[0].nextAppointmentOfficer").isEmpty()
      .jsonPath("$.[0].optionalNumber").isEmpty()
  }

  @Test
  fun `should return array values in a date descending order`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000010",
          dateOfLetter = LocalDate.now(),
          breachConditionTypeDescription = "first_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000010",
          dateOfLetter = LocalDate.now().minusDays(10),
          breachConditionTypeDescription = "second_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000010",
          dateOfLetter = LocalDate.now().plusDays(10),
          breachConditionTypeDescription = "third_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000010").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$.[0].crn").value(containsString("X000010"))
      .jsonPath("$.[0].breachConditionTypeDescription").value(containsString("third_app"))
      .jsonPath("$.[1].crn").value(containsString("X000010"))
      .jsonPath("$.[1].breachConditionTypeDescription").value(containsString("first_app"))
      .jsonPath("$.[2].crn").value(containsString("X000010"))
      .jsonPath("$.[2].breachConditionTypeDescription").value(containsString("second_app"))
  }

  @Test
  fun `should return filtered results based on fromDate & toDate parameters`() {
    val sarParameterDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val twoDaysFromNow = LocalDateTime.now().plusDays(2).format(sarParameterDatePattern)
    val twoDaysAgo = LocalDateTime.now().minusDays(2).format(sarParameterDatePattern)

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000011",
          dateOfLetter = LocalDate.now(),
          breachConditionTypeDescription = "first_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000011",
          dateOfLetter = LocalDate.now().minusDays(10),
          breachConditionTypeDescription = "second_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNotice(
          crn = "X000011",
          dateOfLetter = LocalDate.now().plusDays(10),
          breachConditionTypeDescription = "third_app",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    // Test using only toDate filters results
    webTestClient.get()
      .uri { builder ->
        builder.path("/subject-access-request").queryParam("crn", "X000011")
          .queryParam("toDate", twoDaysFromNow).build()
      }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(2)
      .jsonPath("$.[0].crn").value(containsString("X000011"))
      .jsonPath("$.[0].breachConditionTypeDescription").value(containsString("first_app"))
      .jsonPath("$.[1].crn").value(containsString("X000011"))
      .jsonPath("$.[1].breachConditionTypeDescription").value(containsString("second_app"))

    // Test using only fromDate filters results
    webTestClient.get()
      .uri { builder ->
        builder.path("/subject-access-request").queryParam("crn", "X000011")
          .queryParam("fromDate", twoDaysAgo).build()
      }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(2)
      .jsonPath("$.[0].crn").value(containsString("X000011"))
      .jsonPath("$.[0].breachConditionTypeDescription").value(containsString("third_app"))
      .jsonPath("$.[1].crn").value(containsString("X000011"))
      .jsonPath("$.[1].breachConditionTypeDescription").value(containsString("first_app"))

    // Test using both toDate and fromDate filters results
    webTestClient.get()
      .uri { builder ->
        builder.path("/subject-access-request").queryParam("crn", "X000011")
          .queryParam("toDate", twoDaysFromNow)
          .queryParam("fromDate", twoDaysAgo).build()
      }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(1)
      .jsonPath("$.[0].crn").value(containsString("X000011"))
      .jsonPath("$.[0].breachConditionTypeDescription").value(containsString("first_app"))
  }
}
