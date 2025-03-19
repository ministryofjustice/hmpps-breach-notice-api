package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.AddressEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeRequirementEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.enums.ReviewEventType
import uk.gov.justice.digital.hmpps.breachnoticeapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.CreateResponse
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class BreachNoticeService(
  val breachNoticeRepository: BreachNoticeRepository,
  val pdfGenerationService: PdfGenerationService,
  val sqsService: SnsService,
  @Value("\${frontend.url}") val frontendUrl: String,
) {

  fun createBreachNotice(breachNotice: BreachNotice) = breachNoticeRepository.save(
    breachNotice.toEntity(),
  ).id.let {
    CreateResponse(it, "$frontendUrl/breach-notice/$it")
  }

  @Transactional
  fun updateBreachNotice(id: UUID, breachNotice: BreachNotice): BreachNotice {
    val breachNoticeEntity: BreachNoticeEntity = findBreachNoticeEntity(id)
    return breachNoticeRepository.save(breachNotice.toEntity(breachNoticeEntity)).toModel()
  }

  fun deleteBreachNotice(id: UUID): Any? {
    if (breachNoticeRepository.findByIdOrNull(id) == null) {
      return ResponseEntity(
        "The Breach Notice id was not found",
        HttpStatus.NOT_FOUND,
      )
    }
    return breachNoticeRepository.deleteById(id)
  }

  private fun findBreachNoticeEntity(id: UUID): BreachNoticeEntity = breachNoticeRepository.findByIdOrNull(id) ?: throw NotFoundException("BreachNoticeEntity", "id", id)

  private fun BreachNotice.toEntity(existingEntity: BreachNoticeEntity? = null) = existingEntity?.copy(
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
    nextAppointmentId = nextAppointmentId,
    completedDate = completedDate,
    offenderAddress = offenderAddress?.toEntity(existingEntity.offenderAddress),
    replyAddress = replyAddress?.toEntity(existingEntity.replyAddress),
    basicDetailsSaved = basicDetailsSaved,
    warningTypeSaved = warningTypeSaved,
    warningDetailsSaved = warningDetailsSaved,
    nextAppointmentSaved = nextAppointmentSaved,
    useDefaultAddress = useDefaultAddress,
    useDefaultReplyAddress = useDefaultReplyAddress,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    conditionBeingEnforced = conditionBeingEnforced,
    breachNoticeContactList = breachNoticeContactList.map {
      it.toEntity(
        existingEntity.breachNoticeContactList.find { existingContactEnitiy ->
          existingContactEnitiy.id == it.id
        },
      )
    },
    breachNoticeRequirementList = breachNoticeRequirementList.map {
      it.toEntity(
        existingEntity.breachNoticeRequirementList.find { existingRequirementEntity ->
          existingRequirementEntity.id == it.id
        },
      )
    },
  )?.also { breachNotice ->
    breachNotice.breachNoticeContactList.forEach { it.breachNotice = breachNotice }
    breachNotice.breachNoticeRequirementList.forEach { it.breachNotice = breachNotice }
  } ?: BreachNoticeEntity(
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
    nextAppointmentId = nextAppointmentId,
    completedDate = completedDate,
    offenderAddress = offenderAddress?.toEntity(),
    replyAddress = replyAddress?.toEntity(),
    basicDetailsSaved = basicDetailsSaved,
    warningTypeSaved = warningTypeSaved,
    warningDetailsSaved = warningDetailsSaved,
    nextAppointmentSaved = nextAppointmentSaved,
    useDefaultAddress = useDefaultAddress,
    useDefaultReplyAddress = useDefaultReplyAddress,
    optionalNumberChecked = optionalNumberChecked,
    optionalNumber = optionalNumber,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    breachNoticeRequirementList = breachNoticeRequirementList.map { it.toEntity() },
    breachNoticeContactList = breachNoticeContactList.map { it.toEntity() },
  )

  private fun BreachNoticeEntity.toModel() = BreachNotice(
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
    nextAppointmentId = nextAppointmentId,
    completedDate = completedDate,
    offenderAddress = offenderAddress?.toModel(),
    replyAddress = replyAddress?.toModel(),
    basicDetailsSaved = basicDetailsSaved,
    warningTypeSaved = warningTypeSaved,
    warningDetailsSaved = warningDetailsSaved,
    nextAppointmentSaved = nextAppointmentSaved,
    useDefaultAddress = useDefaultAddress,
    useDefaultReplyAddress = useDefaultReplyAddress,
    breachNoticeContactList = breachNoticeContactList.map { it.toModel() },
    breachNoticeRequirementList = breachNoticeRequirementList.map { it.toModel() },
    optionalNumberChecked = optionalNumberChecked,
    optionalNumber = optionalNumber,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    conditionBeingEnforced = conditionBeingEnforced,
  )

  fun getBreachNoticeById(uuid: UUID) = breachNoticeRepository.findById(uuid).getOrNull()?.let {
    BreachNoticeDetails(
      id = it.id,
      crn = it.crn,
      titleAndFullName = it.titleAndFullName,
      dateOfLetter = it.dateOfLetter,
      referenceNumber = it.referenceNumber,
      responseRequiredDate = it.responseRequiredDate,
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
      nextAppointmentId = it.nextAppointmentId,
      completedDate = it.completedDate,
      offenderAddress = it.offenderAddress?.toModel(),
      replyAddress = it.replyAddress?.toModel(),
      basicDetailsSaved = it.basicDetailsSaved,
      warningTypeSaved = it.warningTypeSaved,
      warningDetailsSaved = it.warningDetailsSaved,
      nextAppointmentSaved = it.nextAppointmentSaved,
      useDefaultAddress = it.useDefaultAddress,
      useDefaultReplyAddress = it.useDefaultReplyAddress,
      breachNoticeContactList = it.breachNoticeContactList.map { it.toModel() },
      breachNoticeRequirementList = it.breachNoticeRequirementList.map { it.toModel() },
      optionalNumberChecked = it.optionalNumberChecked,
      optionalNumber = it.optionalNumber,
      reviewRequiredDate = it.reviewRequiredDate,
      reviewEvent = it.reviewEvent,
      conditionBeingEnforced = it.conditionBeingEnforced,
    )
  }

  private fun AddressEntity.toModel() = Address(
    addressId = addressId,
    officeDescription = officeDescription,
    status = status,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun Address.toEntity(existingEntity: AddressEntity? = null) = existingEntity?.copy(
    addressId = addressId,
    officeDescription = officeDescription,
    status = status,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  ) ?: AddressEntity(
    addressId = addressId,
    status = status,
    officeDescription = officeDescription,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun BreachNoticeContact.toEntity(existingEntity: BreachNoticeContactEntity? = null) = existingEntity?.copy(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
  ) ?: BreachNoticeContactEntity(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
  )

  private fun BreachNoticeContactEntity.toModel() = BreachNoticeContact(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
  )

  private fun BreachNoticeRequirementEntity.toModel() = BreachNoticeRequirement(
    id = id,
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
  )

  private fun BreachNoticeRequirement.toEntity(existingEntity: BreachNoticeRequirementEntity? = null) = existingEntity?.copy(
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
  ) ?: BreachNoticeRequirementEntity(
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
  )

  fun getBreachNoticeAsPdf(id: UUID, breachNoticeDetails: BreachNoticeDetails?, draft: Boolean): ByteArray? {
    val html = pdfGenerationService.generateHtml(breachNoticeDetails)

    var pdfBytes = pdfGenerationService.generatePdf(html)

    if (draft) {
      pdfBytes = pdfGenerationService.addWatermark(pdfBytes)
    }

    return pdfBytes
  }

  fun getActiveBreachNoticesForCrn(crn: String?): Collection<BreachNoticeEntity> = breachNoticeRepository.findByCrnAndCompletedDateIsNull(crn)

  fun updateBreachNoticeCrn(breachNotice: BreachNoticeEntity, crn: String) {
    breachNotice.crn = crn
    breachNoticeRepository.save(breachNotice)
  }

  fun updateReviewEvent(eventType: ReviewEventType, breachNotice: BreachNoticeEntity, occurredAt: LocalDateTime) {
    breachNotice.reviewEvent = eventType.name
    breachNotice.reviewRequiredDate = occurredAt
    breachNoticeRepository.save(breachNotice)
  }

  fun deleteAllByCrn(crn: String) {
    breachNoticeRepository.deleteByCrn(crn)
  }
}
