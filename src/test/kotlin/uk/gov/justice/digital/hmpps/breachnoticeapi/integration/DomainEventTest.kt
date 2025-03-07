package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class DomainEventTest : IntegrationTestBase() {

  @Autowired
  private lateinit var breachNoticeRepository: BreachNoticeRepository

  @Nested
  @DisplayName("GET /breach-notice/{parameter}")
  inner class BreachNoticeTestEntityEndpoint {

    @Test
    fun `merge event should update CRN for active breach notice`() {
      webTestClient.post()
        .uri("/breach-notice")
        .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
        .bodyValue(BreachNotice(crn = "X000101"))
        .exchange()
        .expectStatus()
        .isCreated

      val breachNotice = breachNoticeRepository.findByCrn("X000101").single()
      assertThat(breachNotice.crn).isEqualTo("X000101")
      assertThat(breachNotice.id).isNotNull()

      val message: String = "{\"eventType\":\"probation-case.merge.completed\",\"version\":1,\"occurredAt\":\"2025-03-04T10:30:07.329287Z\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000101\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppsbreachnoticetopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.merge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val breachNoticeUpdated: BreachNoticeEntity = breachNoticeRepository.findById(breachNotice.id).orElse(null)
        assertThat(breachNoticeUpdated).isNotNull
        assertThat(breachNoticeUpdated.crn).isEqualTo("X000102")
        assertThat(breachNoticeUpdated.id).isNotNull()
        assertThat(breachNoticeUpdated.reviewRequiredDate).isNotNull()
        assertThat(breachNoticeUpdated.reviewEvent).isEqualTo("MERGE")
      }
    }

    @Test
    fun `merge event should not update CRN for completed breach notice`() {
      webTestClient.post()
        .uri("/breach-notice")
        .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
        .bodyValue(BreachNotice(crn = "X000111"))
        .exchange()
        .expectStatus()
        .isCreated

      val breachNotice = breachNoticeRepository.findByCrn("X000111").single()
      assertThat(breachNotice.crn).isEqualTo("X000111")
      assertThat(breachNotice.id).isNotNull()

      breachNotice.completedDate = LocalDateTime.now()
      breachNoticeRepository.save(breachNotice)

      val message: String = "{\"eventType\":\"probation-case.merge.completed\",\"version\":1,\"occurredAt\":\"2025-03-04T10:30:07.329287Z\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000111\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppsbreachnoticetopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.merge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val breachNoticeUpdated: BreachNoticeEntity = breachNoticeRepository.findById(breachNotice.id).orElse(null)
        assertThat(breachNoticeUpdated).isNotNull
        assertThat(breachNoticeUpdated.crn).isEqualTo("X000111")
        assertThat(breachNoticeUpdated.id).isNotNull()
        assertThat(breachNoticeUpdated.reviewRequiredDate).isNull()
        assertThat(breachNoticeUpdated.reviewEvent).isNull()
      }
    }

    @Test
    fun `unmerge event should update CRN for active breach notice`() {
      webTestClient.post()
        .uri("/breach-notice")
        .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
        .bodyValue(BreachNotice(crn = "X000121"))
        .exchange()
        .expectStatus()
        .isCreated

      val breachNotice = breachNoticeRepository.findByCrn("X000121").single()
      assertThat(breachNotice.crn).isEqualTo("X000121")
      assertThat(breachNotice.id).isNotNull()

      val message: String = "{\n" +
        "  \"eventType\":\"probation-case.unmerge.completed\",\n" +
        "  \"version\":1,\n" +
        "  \"occurredAt\":\"2025-03-03T12:20:13.6147Z\",\n" +
        "  \"description\":\"An unmerge has been completed on the probation case\",\n" +
        "  \"additionalInformation\":{\n" +
        "    \"reactivatedCRN\":\"X000103\",\n" +
        "    \"unmergedCRN\":\"X000121\"},\n" +
        "  \"personReference\":{\n" +
        "    \"identifiers\":[\n" +
        "      {\n" +
        "        \"type\":\"CRN\",\n" +
        "        \"value\":\"X000121\"\n" +
        "      }\n" +
        "    ]\n" +
        "  }\n" +
        "}"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppsbreachnoticetopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.unmerge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val breachNoticeUpdated: BreachNoticeEntity = breachNoticeRepository.findById(breachNotice.id).orElse(null)
        assertThat(breachNoticeUpdated).isNotNull
        assertThat(breachNoticeUpdated.crn).isEqualTo("X000103")
        assertThat(breachNoticeUpdated.id).isNotNull()
        assertThat(breachNoticeUpdated.reviewRequiredDate).isNotNull()
        assertThat(breachNoticeUpdated.reviewEvent).isEqualTo("UNMERGE")
      }
    }

    @Test
    fun `unmerge event should not update CRN for active breach notice`() {
      webTestClient.post()
        .uri("/breach-notice")
        .headers(setAuthorisation(roles = listOf("ROLE_BREACH_NOTICE")))
        .bodyValue(BreachNotice(crn = "X000131"))
        .exchange()
        .expectStatus()
        .isCreated

      val breachNotice = breachNoticeRepository.findByCrn("X000131").single()
      assertThat(breachNotice.crn).isEqualTo("X000131")
      assertThat(breachNotice.id).isNotNull()

      breachNotice.completedDate = LocalDateTime.now()
      breachNoticeRepository.save(breachNotice)

      val message: String = "{\n" +
        "  \"eventType\":\"probation-case.unmerge.completed\",\n" +
        "  \"version\":1,\n" +
        "  \"occurredAt\":\"2025-03-03T12:20:13.6147Z\",\n" +
        "  \"description\":\"An unmerge has been completed on the probation case\",\n" +
        "  \"additionalInformation\":{\n" +
        "    \"reactivatedCRN\":\"X000103\",\n" +
        "    \"unmergedCRN\":\"X000131\"},\n" +
        "  \"personReference\":{\n" +
        "    \"identifiers\":[\n" +
        "      {\n" +
        "        \"type\":\"CRN\",\n" +
        "        \"value\":\"X000131\"\n" +
        "      }\n" +
        "    ]\n" +
        "  }\n" +
        "}"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppsbreachnoticetopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.unmerge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val breachNoticeUpdated: BreachNoticeEntity = breachNoticeRepository.findById(breachNotice.id).orElse(null)
        assertThat(breachNoticeUpdated).isNotNull
        assertThat(breachNoticeUpdated.crn).isEqualTo("X000131")
        assertThat(breachNoticeUpdated.id).isNotNull()
        assertThat(breachNoticeUpdated.reviewRequiredDate).isNull()
        assertThat(breachNoticeUpdated.reviewEvent).isNull()
      }
    }
  }
}
