package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDate
import java.util.*

data class BreachNoticeRequirement(
  val id: UUID? = null,
  val breachNoticeId: UUID,
  val requirementId: Long,
  val requirementTypeMainCategoryDescription: String? = null,
  val requirementTypeSubCategoryDescription: String? = null,
  val rejectionReason: String? = null,
  val fromDate: LocalDate? = null,
  val toDate: LocalDate? = null,
)
