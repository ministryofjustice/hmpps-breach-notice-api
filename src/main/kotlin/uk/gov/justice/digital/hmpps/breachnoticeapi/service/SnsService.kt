package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.breachnoticeapi.listener.DomainEventsMessage
import uk.gov.justice.digital.hmpps.breachnoticeapi.listener.Identifiers
import uk.gov.justice.digital.hmpps.breachnoticeapi.listener.PersonReference
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class SnsService(
  val hmppsQueueService: HmppsQueueService,
  val objectMapper: ObjectMapper,
  @Value("\${hmpps.sqs.topics.hmppsbreachnoticepublishtopic.arn}") val outboundTopicArn: String,
) {
  fun sendPublishDomainEvent(breachNotice: BreachNotice, id: UUID) {
    val outboundTopic = hmppsQueueService.findByTopicId("hmppsbreachnoticepublishtopic") ?: throw MissingQueueException("HmppsTopic hmppsbreachnoticepublishtopic not found")
    val messageObject = DomainEventsMessage(
      description = "A breach notice has been completed for a person on probation",
      version = 1,
      occurredAt = LocalDateTime.now(),
      eventType = "probation-case.breach-notice.created",
      personReference = PersonReference(listOf(Identifiers(type = "crn", value = breachNotice.crn))),
      detailUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/breach-notice/pdf/" + id,
      additionalInformation = mapOf(
        "breachNoticeId" to id,
      ),
    )
    val publishResponse = outboundTopic.snsClient.publish(
      PublishRequest.builder().topicArn(outboundTopicArn).message(objectMapper.writeValueAsString(messageObject)).messageAttributes(
        mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.breach-notice.created").build()),
      ).build(),
    )

    publishResponse.get(5, TimeUnit.SECONDS).messageId() ?: throw MessagingException("Unable to publish creation message")
  }

  fun sendDeletedDomainEvent(breachNotice: BreachNoticeDetails, id: UUID) {
    val outboundTopic = hmppsQueueService.findByTopicId("hmppsbreachnoticepublishtopic") ?: throw MissingQueueException(
      "HmppsTopic hmppsbreachnoticepublishtopic not found",
    )
    val messageObject = DomainEventsMessage(
      description = "A breach notice has been deleted for a person on probation",
      version = 1,
      occurredAt = LocalDateTime.now(),
      eventType = "probation-case.breach-notice.deleted",
      personReference = PersonReference(listOf(Identifiers(type = "crn", value = breachNotice.crn))),
      detailUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build()
        .toUriString() + "/breach-notice/report-deleted/" + id,
      additionalInformation = mapOf(
        "breachNoticeId" to id,
      ),
    )
    val publishResponse = outboundTopic.snsClient.publish(
      PublishRequest.builder().topicArn(outboundTopicArn).message(objectMapper.writeValueAsString(messageObject))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String")
              .stringValue("probation-case.breach-notice.deleted").build(),
          ),
        ).build(),
    )

    publishResponse.get(5, TimeUnit.SECONDS).messageId()
      ?: throw MessagingException("Unable to publish deletion message")

  }
}
