package uk.gov.justice.digital.hmpps.breachnoticeapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "address")
data class AddressEntity(
  @Id
  val id: UUID? = null,
  val buildingName: String? = null,
  val addressNumber: String? = null,
  val streetName: String? = null,
  val district: String? = null,
  val townCity: String? = null,
  val county: String? = null,
  val postcode: String? = null,
  @CreatedBy
  val createdByUser: String? = null,
  @CreatedDate
  val createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  val lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  val lastUpdatedUser: String? = null,
)
