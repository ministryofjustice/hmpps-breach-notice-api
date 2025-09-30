package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.ContactRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.RequirementRepository
import java.time.LocalDateTime

class BreachNoticeRequirementCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Autowired
  private lateinit var requirementRepository: RequirementRepository

  @Autowired
  private lateinit var contactRepository: ContactRepository

  @Test
  fun `should create a breach notice requirement`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "E000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("E000001").single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 1L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirement = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 1).single()
    assertThat(insertedRequirement.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(insertedRequirement.requirementId).isEqualTo(1L)
    assertThat(insertedRequirement.rejectionReason).isEqualTo("Failed to Comply")
    assertThat(insertedRequirement.requirementTypeMainCategoryDescription).isEqualTo("MainCategory")
    assertThat(insertedRequirement.requirementTypeSubCategoryDescription).isEqualTo("SubCategory")
  }

  @Test
  fun `should update a breach notice requirement`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "E000002"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("E000002").single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 2L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirement = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 2).single()

    webTestClient.put()
      .uri("/requirement/${insertedRequirement.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          id = insertedRequirement.id,
          breachNoticeId = breachNotice.id,
          requirementId = 2L,
          rejectionReason = "Attended Late",
          requirementTypeMainCategoryDescription = "AnotherMainCategory",
          requirementTypeSubCategoryDescription = "AnotherSubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk

    val updatedRequirement = requirementRepository.findById(insertedRequirement.id).get()
    assertThat(updatedRequirement.id).isEqualTo(insertedRequirement.id)
    assertThat(updatedRequirement.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(updatedRequirement.requirementId).isEqualTo(2L)
    assertThat(updatedRequirement.rejectionReason).isEqualTo("Attended Late")
    assertThat(updatedRequirement.requirementTypeMainCategoryDescription).isEqualTo("AnotherMainCategory")
    assertThat(updatedRequirement.requirementTypeSubCategoryDescription).isEqualTo("AnotherSubCategory")
  }

  @Test
  fun `should delete all unlinked requirements`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "E000003"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("E000003").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 2L,
          contactDate = LocalDateTime.now(),
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContact = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 2).single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 3L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 4L,
          rejectionReason = "Missed",
          requirementTypeMainCategoryDescription = "MainCategoryFourth",
          requirementTypeSubCategoryDescription = "SubCategoryFourth",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirementA = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 3).single()
    val insertedRequirementB = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 4).single()

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContact.id,
          requirementId = insertedRequirementB.id,
          contact = null,
          requirement = null,
          id = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    val preDeleteRequirements = requirementRepository.findByBreachNoticeId(breachNotice.id)
    assertThat(preDeleteRequirements.size).isEqualTo(2)

    webTestClient.delete()
      .uri("/requirement/unlinkedrequirements/${breachNotice.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    val postDeleteRequirements = requirementRepository.findByBreachNoticeId(breachNotice.id)
    assertThat(postDeleteRequirements.size).isEqualTo(1)
    assertThat(postDeleteRequirements.first().breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(postDeleteRequirements.first().requirementId).isEqualTo(4L)
    assertThat(postDeleteRequirements.first().rejectionReason).isEqualTo("Missed")
    assertThat(postDeleteRequirements.first().requirementTypeMainCategoryDescription).isEqualTo("MainCategoryFourth")
    assertThat(postDeleteRequirements.first().requirementTypeSubCategoryDescription).isEqualTo("SubCategoryFourth")
  }
}
