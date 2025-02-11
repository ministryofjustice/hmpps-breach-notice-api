package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BreachNoticeService(
  val breachNoticeRepository: BreachNoticeRepository,
  val pdfGenerationService: PdfGenerationService,
  @Value("\${frontend.url}") val frontendUrl: String,
) {

  fun createBreachNotice(breachNotice: BreachNotice) =
    breachNoticeRepository.save(
      breachNotice.toEntity(),
    ).id.let {
      CreateResponse(it, "$frontendUrl/breach-notice/$it")
    }

  fun updateBreachNotice(id: UUID, breachNotice: BreachNotice): Any? {
    val breachNoticeEntity: BreachNoticeEntity = findBreachNoticeEntity(id)

    if (!breachNoticeEntity.crn.equals(breachNotice.crn, ignoreCase = true)) {
      return ResponseEntity(
        "You can not change the CRN in a breach Report",
        HttpStatus.BAD_REQUEST,
      )
    }
    return breachNoticeRepository.save(breachNotice.toEntity(breachNoticeEntity))
  }

  private fun findBreachNoticeEntity(id: UUID): BreachNoticeEntity =
    breachNoticeRepository.findByIdOrNull(id) ?: throw NotFoundException("BreachNoticeEntity", "id", id)

  private fun BreachNotice.toEntity(existingEntity: BreachNoticeEntity? = null) =
    existingEntity?.copy(
      titleAndFullName = titleAndFullName,
      dateOfLetter = dateOfLetter,
      referenceNumber = referenceNumber,
      responseRequiredDate = responseRequiredDate,
      breachNoticeTypeCode = breachNoticeTypeCode,
      breachNoticeTypeDescription = breachNoticeTypeDescription,
      breachConditionTypeCode = breachConditionTypeCode,
      breachConditionTypeDescription = breachConditionTypeDescription,
      breachSentenceTypeCode = breachSentenceTypeCode,
      breachSentenceTypeDescription = breachSentenceTypeDescription,
      responsibleOfficer = responsibleOfficer,
      contactNumber = contactNumber,
      nextAppointmentType = nextAppointmentType,
      nextAppointmentDate = nextAppointmentDate,
      nextAppointmentLocation = nextAppointmentLocation,
      nextAppointmentOfficer = nextAppointmentOfficer,
      nextAppointmentContact = nextAppointmentContact?.toEntity(existingEntity.nextAppointmentContact),
      completedDate = completedDate,
      offenderAddress = offenderAddress?.toEntity(existingEntity.offenderAddress),
      replyAddress = replyAddress?.toEntity(existingEntity.replyAddress),
      basicDetailsSaved = basicDetailsSaved,
      warningTypeSaved = warningTypeSaved,
      warningDetailsSaved = warningDetailsSaved,
      nextAppointmentSaved = nextAppointmentSaved,
      useDefaultAddress = useDefaultAddress,
      useDefaultReplyAddress = useDefaultReplyAddress,
    ) ?: BreachNoticeEntity(
      crn = crn,
      titleAndFullName = titleAndFullName,
      dateOfLetter = dateOfLetter,
      referenceNumber = referenceNumber,
      responseRequiredDate = responseRequiredDate,
      breachNoticeTypeCode = breachNoticeTypeCode,
      breachNoticeTypeDescription = breachNoticeTypeDescription,
      breachConditionTypeCode = breachConditionTypeCode,
      breachConditionTypeDescription = breachConditionTypeDescription,
      breachSentenceTypeCode = breachSentenceTypeCode,
      breachSentenceTypeDescription = breachSentenceTypeDescription,
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
      useDefaultAddress = useDefaultAddress,
      useDefaultReplyAddress = useDefaultReplyAddress,
    )

  private fun BreachNoticeEntity.toModel() =
    BreachNotice(
      crn = crn,
      titleAndFullName = titleAndFullName,
      dateOfLetter = dateOfLetter,
      referenceNumber = referenceNumber,
      responseRequiredDate = responseRequiredDate,
      breachNoticeTypeCode = breachNoticeTypeCode,
      breachNoticeTypeDescription = breachNoticeTypeDescription,
      breachConditionTypeCode = breachConditionTypeCode,
      breachConditionTypeDescription = breachConditionTypeDescription,
      breachSentenceTypeCode = breachSentenceTypeCode,
      breachSentenceTypeDescription = breachSentenceTypeDescription,
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
      useDefaultAddress = useDefaultAddress,
      useDefaultReplyAddress = useDefaultReplyAddress,
    )

  fun getBreachNoticeById(uuid: UUID) = breachNoticeRepository.findById(uuid).getOrNull()?.let {
    BreachNoticeDetails(
      id = it.id,
      crn = it.crn,
      titleAndFullName = it.titleAndFullName,
      dateOfLetter = it.dateOfLetter,
      referenceNumber = it.referenceNumber,
      responseRequiredByDate = it.responseRequiredDate,
      breachNoticeTypeCode = it.breachNoticeTypeCode,
      breachNoticeTypeDescription = it.breachNoticeTypeDescription,
      breachConditionTypeCode = it.breachConditionTypeCode,
      breachConditionTypeDescription = it.breachConditionTypeDescription,
      breachSentenceTypeCode = it.breachSentenceTypeCode,
      breachSentenceTypeDescription = it.breachSentenceTypeDescription,
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
      useDefaultAddress = it.useDefaultAddress,
      useDefaultReplyAddress = it.useDefaultReplyAddress,
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

  fun getBreachNoticeAsPdf(id: UUID, breachNoticeDetails: BreachNoticeDetails?, draft: Boolean): ByteArray? {
    var html = pdfGenerationService.generateHtml(breachNoticeDetails)

    var pdfBytes = pdfGenerationService.generatePdf(html)

    if (draft) {
      pdfBytes = pdfGenerationService.addWatermark(pdfBytes)
    }

    return pdfBytes
  }
}
