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
    val internalContactId = insertedContact.id

    assertThat(insertedContact.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(insertedContact.contactId).isEqualTo(1L)
    assertThat(insertedContact.contactOutcome).isEqualTo("ContactOutcome")
    assertThat(insertedContact.contactDate).isEqualTo(dateTime)
    assertThat(insertedContact.contactType).isEqualTo("ContactType")

    // do the put
    webTestClient.put()
      .uri("/contact/${insertedContact.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 1L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
          wholeSentence = false,
          rejectionReason = "Rejection Reason",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk

    //do a get and internalContactId
    val updatedContact: BreachNoticeContactEntity = contactRepository.findById(internalContactId).get()

    //shouldnt save rejection reason on a non whole sentence contact
    assertThat(updatedContact.rejectionReason).isBlank

    //do a further update where we go from whole sentence false to whole sentence true
    webTestClient.put()
      .uri("/contact/${insertedContact.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          breachNoticeId = breachNotice.id,
          contactId = 1L,
          contactDate = dateTime,
          contactType = "ContactType",
          contactOutcome = "ContactOutcome",
          wholeSentence = true,
          rejectionReason = "Test Rejection Reason",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk

     val nextUpdatedContact: BreachNoticeContactEntity = contactRepository.findById(internalContactId).get()
    //should save rejection reason on a whole sentence contact
    assertThat(nextUpdatedContact.rejectionReason).isNotBlank
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
  fun `should get and update a breach notice contact`() {
    webTestClient.post()
      .uri("/breach-notice")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(BreachNotice(crn = "D000003"))
      .exchange()
      .expectStatus()
      .isCreated

    val breachNotice = breachNoticeRepository.findByCrn("D000003").single()

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

    webTestClient.put()
      .uri("/contact/${insertedContact.id}")
      .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
      .bodyValue(
        BreachNoticeContact(
          id = insertedContact.id,
          breachNoticeId = breachNotice.id,
          contactId = 3L,
          contactDate = LocalDateTime.of(2025, 1, 1, 12, 0),
          contactType = "ContactTypeTwo",
          contactOutcome = "ContactOutcomeTwo",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk

    val updatedContact = contactRepository.findById(insertedContact.id).get()
    assertThat(updatedContact.breachNoticeId).isEqualTo(breachNotice.id)
    assertThat(updatedContact.contactId).isEqualTo(3L)
    assertThat(updatedContact.contactOutcome).isEqualTo("ContactOutcomeTwo")
    assertThat(updatedContact.contactDate).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0))
    assertThat(updatedContact.contactType).isEqualTo("ContactTypeTwo")
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
