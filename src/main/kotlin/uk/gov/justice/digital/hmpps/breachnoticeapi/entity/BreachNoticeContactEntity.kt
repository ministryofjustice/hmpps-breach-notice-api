package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.*
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
  var id: UUID = UUID.randomUUID(),
  @JoinColumn(name = "breach_notice_id", insertable = false, updatable = false)
  @ManyToOne
  var breachNotice: BreachNoticeEntity? = null,
  @Column(name = "breach_notice_id")
  val breachNoticeId: UUID,
  val contactDate: LocalDateTime? = null,
  val contactType: String? = null,
  val contactOutcome: String? = null,
  val contactId: Long,
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
)
