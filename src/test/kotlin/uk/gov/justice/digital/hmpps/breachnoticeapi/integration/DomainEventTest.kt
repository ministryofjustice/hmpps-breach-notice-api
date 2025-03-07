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
  }
}
