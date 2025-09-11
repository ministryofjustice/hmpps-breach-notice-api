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
@Table(name = "contact_requirement")
@EntityListeners(AuditingEntityListener::class)
data class ContactRequirementEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  @JoinColumn(name = "breach_notice_id", insertable = false, updatable = false)
  @ManyToOne
  var breachNotice: BreachNoticeEntity? = null,
  @Column(name = "breach_notice_id")
  val breachNoticeId: UUID,
  @JoinColumn(name = "contact_id", insertable = false, updatable = false)
  @ManyToOne
  var contact: BreachNoticeContactEntity? = null,
  @Column(name = "contact_id")
  var contactId: UUID,
  @JoinColumn(name = "requirement_id", insertable = false, updatable = false)
  @ManyToOne
  var requirement: BreachNoticeRequirementEntity? = null,
  @Column(name = "requirement_id")
  var requirementId: UUID,
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
)
