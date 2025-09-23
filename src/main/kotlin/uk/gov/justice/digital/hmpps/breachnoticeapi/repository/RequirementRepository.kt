package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeRequirementEntity
import java.util.*

@Repository
interface RequirementRepository : JpaRepository<BreachNoticeRequirementEntity, UUID> {
  fun findByBreachNoticeIdAndRequirementId(breachNoticeId: UUID, requirementId: Long): List<BreachNoticeRequirementEntity>
  fun findByBreachNoticeId(breachNoticeId: UUID): List<BreachNoticeRequirementEntity>
}
