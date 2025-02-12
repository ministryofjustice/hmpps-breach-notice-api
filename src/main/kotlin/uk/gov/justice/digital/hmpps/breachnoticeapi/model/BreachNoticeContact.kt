package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDateTime
import java.util.*

data class BreachNoticeContact(
  val id: UUID? = null,
  val contactDate: LocalDateTime? = null,
  val contactType: String? = null,
  val contactOutcome: String? = null,
  val contactId: Long? = null,
)
