package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SARGenerationTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Test
  fun `should return 200 response on valid request`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000001"))
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
      .bodyValue(BreachNotice(crn = "X000002"))
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
      .bodyValue(BreachNotice(crn = "X000003"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000003").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SOMETHING_ELSE")))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return 401 response without a token`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000004"))
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
      .bodyValue(BreachNotice(crn = "X000005"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("prn", "X000005").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isEqualTo(209)
  }

  @Test
  fun `should return 400 response with message when no parameters are passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000006"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("$.userMessage")
      .isEqualTo("One of prn or crn must be supplied.")
  }

  @Test
  fun `should return 209 response when invalid crn format is passed in`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000007"))
      .exchange()
      .expectStatus()
      .isCreated

    // Ideally, an incorrectly formed CRN would throw a 209, however, that cant be implemented at the moment
    // So throws a 204 no content instead as no records would match a malformed crn
    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "MyNewCrn").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isNoContent
  }

  @Test
  fun `should return 400 bad request when date format is incorrect`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000008"))
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
      .isBadRequest
  }

  @Test
  fun `should return correct information fields and clear personal details`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000009"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000009").single()

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice.id)
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
          completedDate = ZonedDateTime.now(),
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
      .isOk

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000009").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(1)
      .jsonPath("$.content.[0].crn").value<String> {
        assertThat(it).contains("X000009")
      }
      .jsonPath("$.content.[0].dateOfLetter").value<String> {
        assertThat(it).contains("2010-01-01")
      }
      .jsonPath("$.content.[0].referenceNumber").value<String> {
        assertThat(it).contains("REFERENCE_NUMBER")
      }
      .jsonPath("$.content.[0].breachNoticeTypeCode").value<String> {
        assertThat(it).contains("BRCH")
      }
      .jsonPath("$.content.[0].breachNoticeTypeDescription").value<String> {
        assertThat(it).contains("BREACH DESCRIPTION")
      }
      .jsonPath("$.content.[0].breachConditionTypeCode").value<String> {
        assertThat(it).contains("TYPE_CODE")
      }
      .jsonPath("$.content.[0].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("CONDITION DESCRIPTION")
      }
      .jsonPath("$.content.[0].breachSentenceTypeCode").value<String> {
        assertThat(it).contains("BR_SNTC")
      }
      .jsonPath("$.content.[0].breachSentenceTypeDescription").value<String> {
        assertThat(it).contains("SENTENCE DESCRIPTION")
      }
      .jsonPath("$.content.[0].responseRequiredDate").value<String> {
        assertThat(it).contains("2012-01-01")
      }
      .jsonPath("$.content.[0].nextAppointmentType").value<String> {
        assertThat(it).contains("NXTTYP")
      }
      .jsonPath("$.content.[0].nextAppointmentDate").value<String> {
        assertThat(it).contains("2010-12-31T10:00:00")
      }
      .jsonPath("$.content.[0].nextAppointmentLocation").value<String> {
        assertThat(it).contains("NXT_LOCATION")
      }
      .jsonPath("$.content.[0].nextAppointmentId").isEqualTo(1234)
      .jsonPath("$.content.[0].completedDate").exists()
      .jsonPath("$.content.[0].offenderAddress.addressId").isEqualTo(25)
      .jsonPath("$.content.[0].offenderAddress.buildingName").value<String> {
        assertThat(it).contains("MOO")
      }
      .jsonPath("$.content.[0].offenderAddress.buildingNumber").value<String> {
        assertThat(it).contains("1")
      }
      .jsonPath("$.content.[0].offenderAddress.streetName").value<String> {
        assertThat(it).contains("strasse")
      }
      .jsonPath("$.content.[0].offenderAddress.district").value<String> {
        assertThat(it).contains("westminster")
      }
      .jsonPath("$.content.[0].offenderAddress.townCity").value<String> {
        assertThat(it).contains("London")
      }
      .jsonPath("$.content.[0].offenderAddress.county").value<String> {
        assertThat(it).contains("Metropolitan")
      }
      .jsonPath("$.content.[0].offenderAddress.postcode").value<String> {
        assertThat(it).contains("AB123CD")
      }
      .jsonPath("$.content.[0].replyAddress.addressId").isEqualTo(1)
      .jsonPath("$.content.[0].replyAddress.buildingName").value<String> {
        assertThat(it).contains("ADDR")
      }
      .jsonPath("$.content.[0].replyAddress.buildingNumber").value<String> {
        assertThat(it).contains("2")
      }
      .jsonPath("$.content.[0].replyAddress.streetName").value<String> {
        assertThat(it).contains("A Street 1")
      }
      .jsonPath("$.content.[0].replyAddress.district").value<String> {
        assertThat(it).contains("The fun district")
      }
      .jsonPath("$.content.[0].replyAddress.townCity").value<String> {
        assertThat(it).contains("NoddyLand")
      }
      .jsonPath("$.content.[0].replyAddress.county").value<String> {
        assertThat(it).contains("Suffolk")
      }
      .jsonPath("$.content.[0].replyAddress.postcode").value<String> {
        assertThat(it).contains("ZY987XW")
      }
      .jsonPath("$.content.[0].basicDetailsSaved").isEqualTo(true)
      .jsonPath("$.content.[0].warningTypeSaved").isEqualTo(true)
      .jsonPath("$.content.[0].warningDetailsSaved").isEqualTo(false)
      .jsonPath("$.content.[0].nextAppointmentSaved").isEqualTo(false)
      .jsonPath("$.content.[0].useDefaultAddress").isEqualTo(true)
      .jsonPath("$.content.[0].useDefaultReplyAddress").isEqualTo(true)
      .jsonPath("$.content.[0].optionalNumberChecked").isEqualTo(false)
      // Cleared information
      .jsonPath("$.content.[0].titleAndFullName").isEmpty()
      .jsonPath("$.content.[0].responsibleOfficer").isEmpty()
      .jsonPath("$.content.[0].contactNumber").isEmpty()
      .jsonPath("$.content.[0].nextAppointmentOfficer").isEmpty()
      .jsonPath("$.content.[0].optionalNumber").isEmpty()
  }

  @Test
  fun `should return array values in a date descending order`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000010"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000010"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000010"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000010")

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[0].id)
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
      .isOk

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[1].id)
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
      .isOk

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[2].id)
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
      .isOk

    webTestClient.get()
      .uri { builder -> builder.path("/subject-access-request").queryParam("crn", "X000010").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(3)
      .jsonPath("$.content.[0].crn").value<String> {
        assertThat(it).contains("X000010")
      }
      .jsonPath("$.content.[0].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("third_app")
      }
      .jsonPath("$.content.[1].crn").value<String> {
        assertThat(it).contains("X000010")
      }
      .jsonPath("$.content.[1].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("first_app")
      }
      .jsonPath("$.content.[2].crn").value<String> {
        assertThat(it).contains("X000010")
      }
      .jsonPath("$.content.[2].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("second_app")
      }
  }

  @Test
  fun `should return filtered results based on fromDate & toDate parameters`() {
    val sarParameterDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val twoDaysFromNow = LocalDate.now().plusDays(2).format(sarParameterDatePattern)
    val twoDaysAgo = LocalDate.now().minusDays(2).format(sarParameterDatePattern)

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000011"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000011"))
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "X000011"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("X000011")

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[0].id)
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
      .isOk

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[1].id)
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
      .isOk

    webTestClient.put()
      .uri("/breach-notice/" + breachNotice[2].id)
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
      .isOk

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
      .jsonPath("$.content.length()").isEqualTo(2)
      .jsonPath("$.content.[0].crn").value<String> {
        assertThat(it).contains("X000011")
      }
      .jsonPath("$.content.[0].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("first_app")
      }
      .jsonPath("$.content.[1].crn").value<String> {
        assertThat(it).contains("X000011")
      }
      .jsonPath("$.content.[1].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("second_app")
      }

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
      .jsonPath("$.content.length()").isEqualTo(2)
      .jsonPath("$.content.[0].crn").value<String> {
        assertThat(it).contains("X000011")
      }
      .jsonPath("$.content.[0].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("third_app")
      }
      .jsonPath("$.content.[1].crn").value<String> {
        assertThat(it).contains("X000011")
      }
      .jsonPath("$.content.[1].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("first_app")
      }

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
      .jsonPath("$.content.length()").isEqualTo(1)
      .jsonPath("$.content.[0].crn").value<String> {
        assertThat(it).contains("X000011")
      }
      .jsonPath("$.content.[0].breachConditionTypeDescription").value<String> {
        assertThat(it).contains("first_app")
      }
  }
}
