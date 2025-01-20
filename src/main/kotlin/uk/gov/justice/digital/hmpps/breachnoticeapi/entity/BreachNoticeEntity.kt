package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.*
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
  @OneToOne
  val nextAppointmentContact: BreachNoticeContactEntity? = null,
  val completedDate: LocalDateTime? = null,

  @CreatedBy
  val createdByUser: String? = null,
  @CreatedDate
  val createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  val lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  val lastUpdatedUser: String? = null,

  @OneToOne
  val offenderAddress: AddressEntity? = null,
  @OneToOne
  val replyAddress: AddressEntity? = null
)
