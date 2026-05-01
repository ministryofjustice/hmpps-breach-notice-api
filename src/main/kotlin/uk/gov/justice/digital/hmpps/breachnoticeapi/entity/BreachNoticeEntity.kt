package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "breach_notice")
@EntityListeners(AuditingEntityListener::class)
data class BreachNoticeEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  var crn: String,
  var titleAndFullName: String? = null,
  var dateOfLetter: LocalDate? = null,
  var referenceNumber: String? = null,
  var responseRequiredDate: LocalDate? = null,
  var breachNoticeTypeCode: String? = null,
  val breachNoticeTypeDescription: String? = null,
  val breachConditionTypeCode: String? = null,
  val breachConditionTypeDescription: String? = null,
  val breachSentenceTypeCode: String? = null,
  val breachSentenceTypeDescription: String? = null,
  var responsibleOfficer: String? = null,
  var contactNumber: String? = null,
  var nextAppointmentType: String? = null,
  var nextAppointmentDate: LocalDateTime? = null,
  var nextAppointmentLocation: String? = null,
  var nextAppointmentOfficer: String? = null,
  var nextAppointmentId: Long? = null,
  var completedDate: ZonedDateTime? = null,
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "offender_address_id", unique = true)
  var offenderAddress: AddressEntity? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "reply_address_id", unique = true)
  var replyAddress: AddressEntity? = null,
  var basicDetailsSaved: Boolean? = null,
  var warningTypeSaved: Boolean? = null,
  var warningDetailsSaved: Boolean? = null,
  var nextAppointmentSaved: Boolean? = null,
  var useDefaultAddress: Boolean? = null,
  var useDefaultReplyAddress: Boolean? = null,
  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "breachNotice")
  val breachNoticeContactList: List<BreachNoticeContactEntity> = emptyList(),
  @OrderBy("requirementTypeMainCategoryDescription")
  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "breachNotice")
  val breachNoticeRequirementList: List<BreachNoticeRequirementEntity> = emptyList(),
  var optionalNumberChecked: Boolean? = null,
  var optionalNumber: String? = null,
  var reviewRequiredDate: LocalDateTime? = null,
  var reviewEvent: String? = null,
  val conditionBeingEnforced: String? = null,
  val selectNextAppointment: Boolean? = null,
  val furtherReasonDetails: String? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "alternate_next_appointment_location", unique = true)
  val alternateNextAppointmentLocation: AddressEntity? = null,
  val alternateNextAppointmentLocationSelected: Boolean? = null,
)
