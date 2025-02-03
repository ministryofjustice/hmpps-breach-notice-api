package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import java.util.*

@Repository
interface ContactRepository : JpaRepository<BreachNoticeContactEntity, UUID>
