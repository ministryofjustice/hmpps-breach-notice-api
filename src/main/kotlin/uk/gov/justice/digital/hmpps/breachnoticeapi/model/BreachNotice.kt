package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDate
import java.time.LocalDateTime

data class BreachNotice(
  val crn: String,
  val dateOfLetter: LocalDate? = null,
  val referenceNumber: String? = null,
  val responseRequiredDate: LocalDate? = null,
  val breachNoticeTypeCode: String? = null,
  val breachConditionTypeCode: String? = null,
  val responsibleOfficer: String? = null,
  val contactNumber: String? = null,
  val nextAppointmentType: String? = null,
  val nextAppointmentDate: LocalDateTime? = null,
  val nextAppointmentLocation: String? = null,
  val nextAppointmentOfficer: String? = null,
  // val nextAppointmentContactId: UUID? =,
  val completedDate: LocalDateTime? = null,
  val offenderAddress: Address? = null,
  val replyAddress: Address? = null,
)
