package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import java.util.*

@Repository
interface BreachNoticeRepository : JpaRepository<BreachNoticeEntity, UUID> {
  fun findByCrn(crn: String): List<BreachNoticeEntity>

  fun findByCrnAndCompletedDateIsNull(crn: String?): List<BreachNoticeEntity>
}
