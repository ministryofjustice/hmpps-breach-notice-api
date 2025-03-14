package uk.gov.justice.digital.hmpps.breachnoticeapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import java.time.LocalDate
import java.util.*

@Repository
interface BreachNoticeRepository : JpaRepository<BreachNoticeEntity, UUID> {
  fun findByCrn(crn: String): List<BreachNoticeEntity>
  fun findByCrnAndCompletedDateIsNull(crn: String?): List<BreachNoticeEntity>
  fun findByCrnAndDateOfLetterBetweenOrderByDateOfLetterDesc(
    crn: String,
    dateOfLetter: LocalDate,
    dateOfLetter2: LocalDate,
  ): List<BreachNoticeEntity>
  fun findByCrnAndDateOfLetterAfterOrderByDateOfLetterDesc(
    crn: String,
    dateOfLetter: LocalDate,
  ): List<BreachNoticeEntity>
  fun findByCrnAndDateOfLetterBeforeOrderByDateOfLetterDesc(
    crn: String,
    dateOfLetter: LocalDate,
  ): List<BreachNoticeEntity>
  fun findByCrnOrderByDateOfLetterDesc(crn: String): List<BreachNoticeEntity>
}
