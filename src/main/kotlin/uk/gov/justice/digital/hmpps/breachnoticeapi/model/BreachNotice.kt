package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDate
import java.time.LocalDateTime

data class BreachNotice(
  val crn: String,
  val dateOfLetter: LocalDate,
  val referenceNumber: String,
  val responseRequiredDate: LocalDate,
  val breachNoticeTypeCode: String,
  val breachConditionTypeCode: String,
  val responsibleOfficer: String,
  val contactNumber: String,
  val nextAppointmentType: String,
  val nextAppointmentDate: LocalDateTime,
  val nextAppointmentLocation: String,
  val nextAppointmentOfficer: String,
  //val nextAppointmentContactId: UUID? =,
  val completedDate: LocalDateTime,
  val offenderAddress: Address? = null,
  val replyAddress: Address? = null,
)
