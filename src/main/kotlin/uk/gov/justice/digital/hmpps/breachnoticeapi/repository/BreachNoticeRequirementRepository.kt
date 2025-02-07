package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeRequirementEntity
import java.util.*

@Repository
interface BreachNoticeRequirementRepository : JpaRepository<BreachNoticeRequirementEntity, UUID>
