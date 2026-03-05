package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class BreachNoticeContactCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Autowired
  private lateinit var contactRepository: ContactRepository

  private final val dateTime = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MILLIS), ZoneOffset.UTC)

  @Test
  fun `should create a breach notice contact`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "D000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("D000001").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 1L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContact = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 1).single()
    assertThat(insertedContact.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(insertedContact.contactId).isEqualTo(1L)
    assertThat(insertedContact.contactOutcome).isEqualTo("ContactOutcome")
    assertThat(insertedContact.contactDate).isEqualTo(dateTime)
    assertThat(insertedContact.contactType).isEqualTo("ContactType")
  }

  @Test
  fun `should update a breach notice contact`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "Z000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("Z000001").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 1L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
          wholeSentence = true,
          rejectionReason = "Rejection Reason",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContact = contactRepository.findByBreachNoticeId(breachNotice.id).single()

    assertThat(insertedContact.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(insertedContact.contactId).isEqualTo(1L)
    assertThat(insertedContact.contactOutcome).isEqualTo("ContactOutcome")
    assertThat(insertedContact.contactDate).isEqualTo(dateTime)
    assertThat(insertedContact.contactType).isEqualTo("ContactType")

    // do the update
    webTestClient.put()
      .uri("/contacts/${insertedContact.breachNoticeId}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        createBreachNoticeContactList(breachNotice.id, insertedContact),
      )
      .exchange()
      .expectStatus()
      .isOk

    val updatedContact = contactRepository.findByBreachNoticeId(breachNotice.id).single()
    assertThat(updatedContact.rejectionReason).isEqualTo("Test 2")
  }

  fun createBreachNoticeContactList(breachNoticeId: UUID, contact: BreachNoticeContactEntity): MutableList<BreachNoticeContactEntity> {
    val breachNoticeContactList: MutableList<BreachNoticeContactEntity> = mutableListOf()

    val breachNoticeContact = BreachNoticeContactEntity(
      id = contact.id,
      breachNoticeId = breachNoticeId,
      contactId = 1L,
      contactDate = dateTime,
      contactType = "ContactType",
      contactOutcome = "ContactOutcome",
      wholeSentence = true,
      rejectionReason = "Test 2",
    )
    breachNoticeContactList.add(breachNoticeContact)
    return breachNoticeContactList
  }

  @Test
  fun `should delete a breach notice contact`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "D000002"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("D000002").single()

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

    webTestClient.delete()
      .uri("/contact/${insertedContact.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk

    val purgedContact = contactRepository.findById(insertedContact.id)
    assertThat(purgedContact.isEmpty)
  }

  @Test
  fun `should batch update breach notice contacts`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "A100002"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("A100002").single()

    // insert a contact
    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 3L,
          contactDate = dateTime,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    val insertedContact = contactRepository.findByBreachNoticeIdAndContactId(breachNotice.id, 3).single()

    // perform a batch update
    webTestClient.put()
      .uri("/contacts/${insertedContact.breachNoticeId}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        createBreachNoticeContactList(breachNotice.id, insertedContact),
      )
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `should fetch a breach notice contact by contact id and breach notice id`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "D000004"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("D000004").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 4L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri("/contact/bybreachnoticeidanddeliusid/${breachNotice.id}/4")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.contactId").isEqualTo(4)
      .jsonPath("$.contactType").value(containsString("ContactType"))
      .jsonPath("$.contactOutcome").value(containsString("ContactOutcome"))
  }

  @Test
  fun `should fetch breach notice contacts by breach notice id`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "D000005"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("D000005").single()

    webTestClient.post()
      .uri("/contact")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 5L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
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
          contactType = "ContactTypeSIX",
          contactOutcome = "ContactOutcomeSIX",
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated

    webTestClient.get()
      .uri("/contact/bybreachnoticeid/${breachNotice.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.[0].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[0].contactId").isEqualTo(5)
      .jsonPath("$.[0].contactType").value(containsString("ContactType"))
      .jsonPath("$.[0].contactOutcome").value(containsString("ContactOutcome"))
      .jsonPath("$.[1].breachNoticeId").value(containsString(breachNotice.id.toString()))
      .jsonPath("$.[1].contactId").isEqualTo(6)
      .jsonPath("$.[1].contactType").value(containsString("ContactTypeSIX"))
      .jsonPath("$.[1].contactOutcome").value(containsString("ContactOutcomeSIX"))
  }
}
