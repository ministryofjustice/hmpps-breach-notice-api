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
  @OneToOne
  val nextAppointmentContact: BreachNoticeContactEntity? = null,
  val completedDate: LocalDateTime,

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
