package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDateTime
import java.util.UUID

data class BreachNoticeContact(
  val id: UUID? = null,
  val breachNoticeId: UUID,
  val contactDate: LocalDateTime? = null,
  val contactType: String? = null,
  val contactOutcome: String? = null,
  val contactId: Long,
  val wholeSentence: Boolean? = null,
  var rejectionReason: String? = null,
)
