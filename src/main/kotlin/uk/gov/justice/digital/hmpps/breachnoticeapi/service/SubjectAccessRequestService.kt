package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService(
  val breachNoticeService: BreachNoticeService,
) : HmppsProbationSubjectAccessRequestService {

  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val sarData = breachNoticeService.getAllForSARByCRN(crn, fromDate, toDate)

    if (sarData.isEmpty()) {
      return null
    }

    return HmppsSubjectAccessRequestContent(sarData)
  }
}
