package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "breach_notice_contact")
@EntityListeners(AuditingEntityListener::class)
data class BreachNoticeContactEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  val breachNoticeId: UUID,
  val contactDate: LocalDateTime? = null,
  val contactType: String? = null,
  val contactOutcome: String? = null,
  val contactId: Long? = null,
  @CreatedBy
  val createdByUser: String? = null,
  @CreatedDate
  val createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  val lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  val lastUpdatedUser: String? = null,
)
