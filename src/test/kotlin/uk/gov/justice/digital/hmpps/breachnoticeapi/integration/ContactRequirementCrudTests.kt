package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.ContactRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRequirementRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.RequirementRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ContactRequirementCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Autowired
  private lateinit var contactRepository: ContactRepository

  @Autowired
  private lateinit var requirementRepository: RequirementRepository

  @Autowired
  private lateinit var contactRequirementRepository: ContactRequirementRepository

  private final val dateTime = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MILLIS), ZoneOffset.UTC)

  @Test
  fun `should batch create & update a list of contact requirement links`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "F000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("F000001").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 1L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContact = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 1).single()

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

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContact.id,
          requirementId = insertedRequirement.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    val crlinks = contactRequirementRepository.findByBreachNoticeId(breachNotice.id)
    assertThat(crlinks.size).isEqualTo(1)
    assertThat(crlinks.first().requirementId).isEqualTo(insertedRequirement.id)
    assertThat(crlinks.first().contactId).isEqualTo(insertedContact.id)
    assertThat(crlinks.first().breachNoticeId).isEqualTo(breachNotice.id)
  }

  @Test
  fun `should delete Contact Requirement links not passed into the request body`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "F000002"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("F000002").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 2L,
          contactDate = dateTime,
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
          requirementId = 2L,
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
          requirementId = 3L,
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
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirementA = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 2).single()
    val insertedRequirementB = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 3).single()
    val insertedRequirementC = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 4).single()

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContact.id,
          requirementId = insertedRequirementA.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

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
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    val crlinks = contactRequirementRepository.findByBreachNoticeId(breachNotice.id)
    assertThat(crlinks.size).isEqualTo(2)
    val crlinkToDelete = contactRequirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, insertedRequirementA.id).first()

    webTestClient.delete()
      .uri("/crlinks/${crlinkToDelete.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    val refreshedCrlinks = contactRequirementRepository.findByBreachNoticeId(breachNotice.id)
    assertThat(refreshedCrlinks.size).isEqualTo(1)

    val purgedLink = contactRequirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, insertedRequirementA.id)
    assertThat(purgedLink.isEmpty())
  }

  @Test
  fun `should fetch contact-requirements links by breach notice id`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "F000003"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("F000003").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 5L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 6L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContactA = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 5).single()
    val insertedContactB = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 6).single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 5L,
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
          requirementId = 6L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirementA = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 5).single()
    val insertedRequirementB = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 6).single()

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactA.id,
          requirementId = insertedRequirementA.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactB.id,
          requirementId = insertedRequirementB.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri("/crlinks/bybreachnoticeid/${breachNotice.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.[0].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[0].contactId").value(containsString(insertedContactA.id.toString()))
      .jsonPath("$.[0].requirementId").value(containsString(insertedRequirementA.id.toString()))
      .jsonPath("$.[1].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[1].contactId").value(containsString(insertedContactB.id.toString()))
      .jsonPath("$.[1].requirementId").value(containsString(insertedRequirementB.id.toString()))
  }

  @Test
  fun `should fetch contact-requirement links by by contact id and breach notice id`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "F000004"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("F000004").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 5L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 6L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContactA = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 5).single()
    val insertedContactB = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 6).single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 5L,
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
          requirementId = 6L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirementA = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 5).single()
    val insertedRequirementB = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 6).single()

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactA.id,
          requirementId = insertedRequirementA.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactB.id,
          requirementId = insertedRequirementA.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactB.id,
          requirementId = insertedRequirementB.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri("/crlinks/bybreachnoticeidandcontactid/${breachNotice.id}/${insertedContactB.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.[0].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[0].contactId").value(containsString(insertedContactB.id.toString()))
      .jsonPath("$.[0].requirementId").value(containsString(insertedRequirementA.id.toString()))
      .jsonPath("$.[1].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[1].contactId").value(containsString(insertedContactB.id.toString()))
      .jsonPath("$.[1].requirementId").value(containsString(insertedRequirementB.id.toString()))
  }

  @Test
  fun `should recalculate from & toDate of requirement after removing contact-requirements`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "F000005"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("F000005").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 7L,
          contactDate = LocalDateTime.of(2025, 1, 1, 12, 59),
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 8L,
          contactDate = LocalDateTime.of(2024, 12, 31, 10, 50),
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContactA = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 7).single()
    val insertedContactB = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 8).single()

    webTestClient.post()
      .uri("/requirement")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeRequirement(
          breachNoticeId = breachNotice.id,
          requirementId = 7L,
          rejectionReason = "Failed to Comply",
          requirementTypeMainCategoryDescription = "MainCategory",
          requirementTypeSubCategoryDescription = "SubCategory",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedRequirement = requirementRepository.findByBreachNoticeIdAndRequirementId(breachNotice.id, 7).single()

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactA.id,
          requirementId = insertedRequirement.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    webTestClient.post()
      .uri("/crlinks")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        ContactRequirement(
          breachNoticeId = breachNotice.id,
          contactId = insertedContactB.id,
          requirementId = insertedRequirement.id,
          contact = null,
          requirement = null,
        ),
      ).exchange()
      .expectStatus()
      .isCreated

    val postLinkRequirement = requirementRepository.findById(insertedRequirement.id).get()
    assertThat(postLinkRequirement.fromDate == LocalDateTime.of(2024, 12, 31, 10, 50))
    assertThat(postLinkRequirement.toDate == LocalDateTime.of(2025, 1, 1, 12, 59))

    val existingCrLinks = contactRequirementRepository.findByBreachNoticeIdAndContactId(breachNotice.id, insertedContactA.id).first()

    webTestClient.delete()
      .uri("/crlinks/${existingCrLinks.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    val postUpdateRequirement = requirementRepository.findById(insertedRequirement.id).get()
    assertThat(postUpdateRequirement.fromDate == LocalDateTime.of(2024, 12, 31, 10, 50))
    assertThat(postUpdateRequirement.toDate == LocalDateTime.of(2024, 12, 31, 10, 50))
  }
}
