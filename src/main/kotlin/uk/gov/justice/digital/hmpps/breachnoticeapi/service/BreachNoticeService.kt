package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.AddressEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.CreateResponse
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.AddressRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BreachNoticeService(
  val breachNoticeRepository: BreachNoticeRepository,
  val addressRepository: AddressRepository,
  @Value("\${frontend.url}") val frontendUrl: String,
  private val contactRepository: ContactRepository,
) {

  fun createBreachNotice(breachNotice: BreachNotice) =
    breachNoticeRepository.save(
      breachNotice.toEntity(),
    ).id.let {
      CreateResponse(it, "$frontendUrl/breach-notice/$it")
    }

  fun updateBreachNotice(id: UUID, breachNotice: BreachNotice): BreachNoticeEntity {
    val breachNoticeEntity: BreachNoticeEntity = findBreachNoticeEntity(id)
    breachNoticeEntity.titleAndFullName = breachNotice.titleAndFullName
    breachNoticeEntity.dateOfLetter = breachNotice.dateOfLetter
    breachNoticeEntity.referenceNumber = breachNotice.referenceNumber
    breachNoticeEntity.responseRequiredDate = breachNotice.responseRequiredDate
    breachNoticeEntity.breachNoticeTypeCode = breachNotice.breachNoticeTypeCode
    breachNoticeEntity.breachConditionTypeCode = breachNotice.breachConditionTypeCode
    breachNoticeEntity.responsibleOfficer = breachNotice.responsibleOfficer
    breachNoticeEntity.contactNumber = breachNotice.contactNumber
    breachNoticeEntity.nextAppointmentType = breachNotice.nextAppointmentType
    breachNoticeEntity.nextAppointmentDate = breachNotice.nextAppointmentDate
    breachNoticeEntity.nextAppointmentLocation = breachNotice.nextAppointmentLocation
    breachNoticeEntity.nextAppointmentOfficer = breachNotice.nextAppointmentOfficer
    breachNoticeEntity.completedDate = breachNotice.completedDate
    breachNoticeEntity.offenderAddress = breachNotice.offenderAddress?.toEntity(breachNoticeEntity.offenderAddress)
    breachNoticeEntity.replyAddress = breachNotice.replyAddress?.toEntity(breachNoticeEntity.replyAddress)
    breachNoticeEntity.nextAppointmentContact = breachNotice.nextAppointmentContact?.toEntity(breachNoticeEntity.nextAppointmentContact)
    breachNoticeEntity.basicDetailsSaved = breachNotice.basicDetailsSaved
    breachNoticeEntity.warningTypeSaved = breachNotice.warningTypeSaved
    breachNoticeEntity.warningDetailsSaved = breachNotice.warningDetailsSaved
    breachNoticeEntity.nextAppointmentSaved = breachNotice.nextAppointmentSaved
    return breachNoticeRepository.save(breachNoticeEntity);
  }

  private fun findBreachNoticeEntity(id: UUID): BreachNoticeEntity =
    breachNoticeRepository.findByIdOrNull(id) ?: throw NotFoundException("BreachNoticeEntity", "id", id)

  private fun BreachNotice.toEntity() =
    BreachNoticeEntity(
      crn = crn,
      dateOfLetter = dateOfLetter,
      referenceNumber = referenceNumber,
      responseRequiredDate = responseRequiredDate,
      breachNoticeTypeCode = breachNoticeTypeCode,
      breachConditionTypeCode = breachConditionTypeCode,
      responsibleOfficer = responsibleOfficer,
      contactNumber = contactNumber,
      nextAppointmentType = nextAppointmentType,
      nextAppointmentDate = nextAppointmentDate,
      nextAppointmentLocation = nextAppointmentLocation,
      nextAppointmentOfficer = nextAppointmentOfficer,
      nextAppointmentContact = nextAppointmentContact?.toEntity(),
      completedDate = completedDate,
      offenderAddress = offenderAddress?.toEntity(),
      replyAddress = replyAddress?.toEntity(),
      basicDetailsSaved = basicDetailsSaved,
      warningTypeSaved = warningTypeSaved,
      warningDetailsSaved = warningDetailsSaved,
      nextAppointmentSaved = nextAppointmentSaved,
    )

  private fun BreachNoticeEntity.toModel() =
    BreachNotice(
      crn = crn,
      dateOfLetter = dateOfLetter,
      referenceNumber = referenceNumber,
      responseRequiredDate = responseRequiredDate,
      breachNoticeTypeCode = breachNoticeTypeCode,
      breachConditionTypeCode = breachConditionTypeCode,
      responsibleOfficer = responsibleOfficer,
      contactNumber = contactNumber,
      nextAppointmentType = nextAppointmentType,
      nextAppointmentDate = nextAppointmentDate,
      nextAppointmentLocation = nextAppointmentLocation,
      nextAppointmentOfficer = nextAppointmentOfficer,
      nextAppointmentContact = nextAppointmentContact?.toModel(),
      completedDate = completedDate,
      offenderAddress = offenderAddress?.toModel(),
      replyAddress = replyAddress?.toModel(),
      basicDetailsSaved = basicDetailsSaved,
      warningTypeSaved = warningTypeSaved,
      warningDetailsSaved = warningDetailsSaved,
      nextAppointmentSaved = nextAppointmentSaved,
    )


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
      basicDetailsSaved = it.basicDetailsSaved,
      warningTypeSaved = it.warningTypeSaved,
      warningDetailsSaved = it.warningDetailsSaved,
      nextAppointmentSaved = it.nextAppointmentSaved,
    )
  }

  private fun AddressEntity.toModel() = Address(
    addressId = addressId,
    type = type,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun Address.toEntity(existingEntity: AddressEntity? = null) =
    existingEntity?.copy(
      addressId = addressId,
      type = type,
      buildingName = buildingName,
      addressNumber = addressNumber,
      streetName = streetName,
      district = district,
      townCity = townCity,
      county = county,
      postcode = postcode,
    ) ?: AddressEntity(
      addressId = addressId,
      type = type,
      buildingName = buildingName,
      addressNumber = addressNumber,
      streetName = streetName,
      district = district,
      townCity = townCity,
      county = county,
      postcode = postcode,
    )


  private fun BreachNoticeContact.toEntity(existingEntity: BreachNoticeContactEntity? = null) =
    existingEntity?.copy(
      breachNoticeId = breachNoticeId,
      contactDate = contactDate,
      contactType = contactType,
      contactOutcome = contactOutcome,
      contactId = contactId,
    ) ?: BreachNoticeContactEntity(
      breachNoticeId = breachNoticeId,
      contactDate = contactDate,
      contactType = contactType,
      contactOutcome = contactOutcome,
      contactId = contactId,
    )

  private fun BreachNoticeContactEntity.toModel() = BreachNoticeContact(
    breachNoticeId = breachNoticeId,
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
  )
}
