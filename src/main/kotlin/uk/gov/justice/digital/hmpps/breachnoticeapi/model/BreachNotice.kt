package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

data class BreachNotice(
  @field:NotBlank(message = "CRN must not be blank")
  @field:Size(min = 7, max = 7, message = "CRN must be 7 characters long")
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
  val breachNoticeContactList: List<BreachNoticeContact> = emptyList(),
  val breachNoticeRequirementList: List<BreachNoticeRequirement> = emptyList(),
)
