package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class BreachNoticeDetails(
  val id: UUID?,
  val crn: String,
  val titleAndFullName: String? = null,
  val dateOfLetter: LocalDate? = null,
  val referenceNumber: String? = null,
  val responseRequiredByDate: LocalDate? = null,
  val breachNoticeTypeCode: String? = null,
  val breachNoticeTypeDescription: String? = null,
  val breachConditionTypeCode: String? = null,
  val breachConditionTypeDescription: String? = null,
  val breachSentenceTypeCode: String? = null,
  val breachSentenceTypeDescription: String? = null,
  val responsibleOfficer: String? = null,
  val contactNumber: String? = null,
  val nextAppointmentType: String? = null,
  val nextAppointmentDate: LocalDateTime? = null,
  val nextAppointmentLocation: String? = null,
  val nextAppointmentOfficer: String? = null,
  val nextAppointmentContact: BreachNoticeContact? = null,
  val completedDate: LocalDateTime? = null,
  val offenderAddress: Address? = null,
  val replyAddress: Address? = null,
  val basicDetailsSaved: Boolean? = null,
  val warningTypeSaved: Boolean? = null,
  val warningDetailsSaved: Boolean? = null,
  val nextAppointmentSaved: Boolean? = null,
  val useDefaultAddress: Boolean? = null,
  val useDefaultReplyAddress: Boolean? = null,
)
