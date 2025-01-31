package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "breach_notice")
@EntityListeners(AuditingEntityListener::class)
data class BreachNoticeEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  val crn: String,
  var titleAndFullName: String? = null,
  var dateOfLetter: LocalDate? = null,
  var referenceNumber: String? = null,
  var responseRequiredDate: LocalDate? = null,
  var breachNoticeTypeCode: String? = null,
  var breachConditionTypeCode: String? = null,
  var responsibleOfficer: String? = null,
  var contactNumber: String? = null,
  var nextAppointmentType: String? = null,
  var nextAppointmentDate: LocalDateTime? = null,
  var nextAppointmentLocation: String? = null,
  var nextAppointmentOfficer: String? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "next_appointment_contact_id", unique = true)
  var nextAppointmentContact: BreachNoticeContactEntity? = null,
  var completedDate: LocalDateTime? = null,
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
)
