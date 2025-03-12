package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService(
  val breachNoticeRepository: BreachNoticeRepository,
) : HmppsProbationSubjectAccessRequestService {

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val sarData = if (fromDate != null && toDate != null) {
      breachNoticeRepository.findByCrnAndDateOfLetterBetweenOrderByDateOfLetterDesc(
        crn,
        fromDate,
        toDate,
      )
    } else if (fromDate != null) {
      breachNoticeRepository.findByCrnAndDateOfLetterAfterOrderByDateOfLetterDesc(
        crn,
        fromDate,
      )
    } else if (toDate != null) {
      breachNoticeRepository.findByCrnAndDateOfLetterBeforeOrderByDateOfLetterDesc(
        crn,
        toDate,
      )
    } else {
      breachNoticeRepository.findByCrnOrderByDateOfLetterDesc(crn)
    }

    if (sarData.isEmpty()) {
      return null
    }

    // Clear Identifiable user information from the breach notice
    for (bn in sarData) {
      bn.titleAndFullName = null
      bn.optionalNumber = null
      bn.contactNumber = null
      bn.responsibleOfficer = null
      bn.nextAppointmentOfficer = null
    }

    return HmppsSubjectAccessRequestContent(sarData)
  }
}
