package uk.gov.justice.digital.hmpps.breachnoticeapi.listener
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.enums.ReviewEventType
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeService
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.NDeliusIntegrationService
import java.time.ZonedDateTime

@Service
class DomainEventsListener(
  private val breachNoticeService: BreachNoticeService,
  private val objectMapper: ObjectMapper,
  private val nDeliusIntegrationService: NDeliusIntegrationService,
) {

  @Transactional
  @SqsListener("hmppsbreachnoticequeue", factory = "hmppsQueueContainerFactoryProxy")
  fun listen(msg: String) {
    val (message, attributes) = objectMapper.readValue<SQSMessage>(msg)
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(message)
    handleMessage(domainEventMessage)
  }

  private fun handleMessage(message: DomainEventsMessage) {
    when (message.eventType) {
      "probation-case.merge.completed" -> {
        // Update CRNs where appropriate
        val breachNotices = breachNoticeService.getActiveBreachNoticesForCrn(message.sourceCrn)
        breachNotices.forEach {
          breachNoticeService.updateBreachNoticeCrn(it, requireNotNull(message.targetCrn))
        }

        updateReviewEvent(ReviewEventType.MERGE, breachNotices, message.occurredAt)
      }

      "probation-case.unmerge.completed" -> {
        // Update CRNs where appropriate
        val breachNotices = breachNoticeService.getActiveBreachNoticesForCrn(message.unmergedCrn)
        breachNotices.forEach {
          nDeliusIntegrationService.getCrnForBreachNoticeUuid(it.id.toString())?.crn?.let { crn ->
            breachNoticeService.updateBreachNoticeCrn(
              it,
              crn,
            )
          }
        }

        updateReviewEvent(ReviewEventType.UNMERGE, breachNotices, message.occurredAt)
      }

      "probation-case.sentence.moved" -> {
        // Update CRNs where appropriate
        val breachNotices = breachNoticeService.getActiveBreachNoticesForCrn(message.sourceCrn)
        breachNotices.forEach {
          nDeliusIntegrationService.getCrnForBreachNoticeUuid(it.id.toString())?.crn?.let { crn ->
            breachNoticeService.updateBreachNoticeCrn(
              it,
              crn,
            )
          }
        }

        updateReviewEvent(ReviewEventType.EVENT_MOVE, breachNotices, message.occurredAt)
      }

      "probation-case.deleted.gdpr" -> {
        message.crn?.let { breachNoticeService.deleteAllByCrn(it) }
      }
    }
  }

  private fun updateReviewEvent(eventType: ReviewEventType, breachNotices: Collection<BreachNoticeEntity>, occurredAt: ZonedDateTime) {
    breachNotices.forEach { breachNotice -> breachNoticeService.updateReviewEvent(eventType, breachNotice, occurredAt) }
  }
}

data class DomainEventsMessage(
  val eventType: String,
  val description: String,
  val personReference: PersonReference,
  val occurredAt: ZonedDateTime,
  val version: Long?,
  val detailUrl: String?,
  val additionalInformation: Map<String, Any>? = mapOf(),
) {
  @JsonIgnore
  val crn = personReference.identifiers.firstOrNull { it.type == "CRN" }?.value

  @JsonIgnore
  val sourceCrn = additionalInformation?.get("sourceCRN") as String?

  @JsonIgnore
  val targetCrn = additionalInformation?.get("targetCRN") as String?

  @JsonIgnore
  val unmergedCrn = additionalInformation?.get("unmergedCRN") as String?

  @JsonIgnore
  val reactivatedCrn = additionalInformation?.get("reactivatedCRN") as String?
}

data class PersonReference(
  val identifiers: List<Identifiers>,
)

data class Identifiers(
  val type: String,
  val value: String,
)

data class SQSMessage(
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
)

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {

  val eventType = attributes[EVENT_TYPE_KEY]?.value

  companion object {
    private const val EVENT_TYPE_KEY = "eventType"
  }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
