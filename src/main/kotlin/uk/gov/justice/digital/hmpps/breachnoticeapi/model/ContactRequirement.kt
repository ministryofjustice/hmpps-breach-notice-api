package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.util.UUID

data class ContactRequirement(
  val breachNoticeId: UUID,
  val contact: BreachNoticeContact?,
  val contactId: UUID,
  val requirement: BreachNoticeRequirement?,
  val requirementId: UUID,
)
