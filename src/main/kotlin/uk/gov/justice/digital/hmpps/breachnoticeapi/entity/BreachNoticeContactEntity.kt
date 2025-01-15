package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "breach_notice_contact")
data class BreachNoticeContactEntity(
  @Id
  val id: UUID? = null,
  @ManyToOne
  val breachNotice: BreachNoticeEntity,
  val contactDate: LocalDateTime,
  val contactType: String,
  val contactOutcome: String,
  val contactId: Long,
  @CreatedBy
  val createdByUser: String? = null,
  @CreatedDate
  val createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  val lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  val lastUpdatedUser: String? = null,
)
