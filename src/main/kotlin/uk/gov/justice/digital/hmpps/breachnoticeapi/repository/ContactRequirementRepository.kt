package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.ContactRequirementEntity
import java.util.*

@Repository
interface ContactRequirementRepository : JpaRepository<ContactRequirementEntity, UUID> {
  fun findByBreachNoticeId(breachNoticeId: UUID): List<ContactRequirementEntity>
  fun findByBreachNoticeIdAndContactId(breachNoticeId: UUID, contactId: UUID): List<ContactRequirementEntity>
}
