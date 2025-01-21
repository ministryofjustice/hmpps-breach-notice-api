package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.AddressEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.CreateResponse
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BreachNoticeService(val breachNoticeRepository: BreachNoticeRepository, @Value("\${frontend.url}") val frontendUrl: String) {

  fun createBreachNotice(breachNotice: BreachNotice) =
    breachNoticeRepository.save(
      BreachNoticeEntity(
        crn = breachNotice.crn,
        dateOfLetter = breachNotice.dateOfLetter,
        referenceNumber = breachNotice.referenceNumber,
        responseRequiredDate = breachNotice.responseRequiredDate,
        breachNoticeTypeCode = breachNotice.breachNoticeTypeCode,
        breachConditionTypeCode = breachNotice.breachConditionTypeCode,
        responsibleOfficer = breachNotice.responsibleOfficer,
        contactNumber = breachNotice.contactNumber,
        nextAppointmentType = breachNotice.nextAppointmentType,
        nextAppointmentDate = breachNotice.nextAppointmentDate,
        nextAppointmentLocation = breachNotice.nextAppointmentLocation,
        nextAppointmentOfficer = breachNotice.nextAppointmentOfficer,
        completedDate = breachNotice.completedDate,
        offenderAddress = breachNotice.offenderAddress?.toEntity(),
        replyAddress = breachNotice.replyAddress?.toEntity(),
      ),
    ).id.let { CreateResponse(it, "$frontendUrl/breach-notice?uuid=$it") }

  fun getBreachNoticeById(uuid: UUID) = breachNoticeRepository.findById(uuid).getOrNull()?.let {
    BreachNoticeDetails(
      id = it.id,
      crn = it.crn,
      dateOfLetter = it.dateOfLetter,
      referenceNumber = it.referenceNumber,
      responseRequiredByDate = it.responseRequiredDate,
      breachNoticeTypeCode = it.breachNoticeTypeCode,
      breachConditionTypeCode = it.breachConditionTypeCode,
      responsibleOfficer = it.responsibleOfficer,
      contactNumber = it.contactNumber,
      nextAppointmentType = it.nextAppointmentType,
      nextAppointmentDate = it.nextAppointmentDate,
      nextAppointmentLocation = it.nextAppointmentLocation,
      nextAppointmentOfficer = it.nextAppointmentOfficer,

      completedDate = it.completedDate,
      offenderAddress = it.offenderAddress?.toModel(),
      replyAddress = it.replyAddress?.toModel(),
    )
  }

  private fun AddressEntity.toModel() = Address(
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun Address.toEntity() = AddressEntity(
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )
}
