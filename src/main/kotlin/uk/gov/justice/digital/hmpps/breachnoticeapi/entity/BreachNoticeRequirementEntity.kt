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
@Table(name = "breach_notice_requirement")
@EntityListeners(AuditingEntityListener::class)
data class BreachNoticeRequirementEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  val breachNoticeId: UUID,
  val requirementId: Long,
  val requirementTypeMainCategoryDescription: String? = null,
  val requirementTypeSubCategoryDescription: String? = null,
  val rejectionReason: String? = null,
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
)
