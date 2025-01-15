package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class BreachNoticeDetails(
  val id: UUID,
  val crn: String,
  val dateOfLetter: LocalDate,
  val referenceNumber: String,
  val responseRequiredByDate: LocalDate,
  val breachNoticeTypeCode: String,
  val breachConditionTypeCode: String,
  val responsibleOfficer: String,
  val contactNumber: String,
  val nextAppointmentType: String,
  val nextAppointmentDate: LocalDateTime,
  val nextAppointmentLocation: String,
  val nextAppointmentOfficer: String,
//  val nextAppointmentContactId: UUID,
  val completedDate: LocalDateTime,
  val offenderAddress: Address?,
  val replyAddress: Address?,
)
