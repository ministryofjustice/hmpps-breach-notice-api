package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class BreachNotice(
  @field:Pattern(regexp = "^[A-Z][0-9]{6}")
  val crn: String,
  val titleAndFullName: String? = null,
  val dateOfLetter: LocalDate? = null,
  val referenceNumber: String? = null,
  val responseRequiredDate: LocalDate? = null,
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
  val nextAppointmentId: Long? = null,
  val completedDate: ZonedDateTime? = null,
  val offenderAddress: Address? = null,
  val replyAddress: Address? = null,
  val basicDetailsSaved: Boolean? = null,
  val warningTypeSaved: Boolean? = null,
  val warningDetailsSaved: Boolean? = null,
  val nextAppointmentSaved: Boolean? = null,
  val useDefaultAddress: Boolean? = null,
  val useDefaultReplyAddress: Boolean? = null,
  @field:JsonSetter(nulls = Nulls.AS_EMPTY)
  val breachNoticeContactList: List<BreachNoticeContact> = emptyList(),
  @field:JsonSetter(nulls = Nulls.AS_EMPTY)
  val breachNoticeRequirementList: List<BreachNoticeRequirement> = emptyList(),
  val optionalNumberChecked: Boolean? = null,
  val optionalNumber: String? = null,
  var reviewRequiredDate: LocalDateTime? = null,
  var reviewEvent: String? = null,
  val conditionBeingEnforced: String? = null,
  val selectNextAppointment: Boolean? = null,
  val furtherReasonDetails: String? = null,
  val alternateNextAppointmentLocation: Address? = null,
  val alternateNextAppointmentLocationSelected: Boolean? = null,
)
