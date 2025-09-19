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
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.ContactRequirementEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.enums.ReviewEventType
import uk.gov.justice.digital.hmpps.breachnoticeapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.ContactRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.CreateResponse
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.InitialiseBreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRequirementRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.RequirementRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class BreachNoticeService(
  val breachNoticeRepository: BreachNoticeRepository,
  val pdfGenerationService: PdfGenerationService,
  val contactRepository: ContactRepository,
  val contactRequirementRepository: ContactRequirementRepository,
  @Value("\${frontend.url}") val frontendUrl: String,
  private val requirementRepository: RequirementRepository,
) {

  fun createBreachNotice(initialiseBreachNotice: InitialiseBreachNotice) = breachNoticeRepository.save(
    BreachNoticeEntity(crn = initialiseBreachNotice.crn),
  ).id.let {
    CreateResponse(it, "$frontendUrl/breach-notice/$it")
  }

  @Transactional
  fun updateBreachNotice(id: UUID, breachNotice: BreachNotice): BreachNotice {
    val breachNoticeEntity: BreachNoticeEntity = findBreachNoticeEntity(id)
    return breachNoticeRepository.save(breachNotice.toEntity(breachNoticeEntity)).toModel()
  }

  @Transactional
  fun deleteBreachNotice(id: UUID): Any? {
    if (breachNoticeRepository.findByIdOrNull(id) == null) {
      return ResponseEntity(
        "The Breach Notice id was not found",
        HttpStatus.NOT_FOUND,
      )
    }
    return breachNoticeRepository.deleteById(id)
  }

  @Transactional
  fun updateBreachNoticeContacts(id: UUID, breachNoticeContacts: List<BreachNoticeContact>): List<BreachNoticeContact> {
    breachNoticeContacts.forEach { breachNoticeContact ->
      val contactList = contactRepository.findByBreachNoticeIdAndContactId(id, breachNoticeContact.contactId)
      if (contactList.isEmpty()) {
        val contact = breachNoticeContact.toEntity()
        contactRepository.save(contact)
      }
    }
    return contactRepository.findByBreachNoticeId(id).map { it.toModel() }
  }

  fun fetchBreachNoticeContact(id: UUID, contactId: Long): BreachNoticeContact = contactRepository.findFirstByBreachNoticeIdAndContactId(id, contactId).toModel()

  @Transactional
  fun updateBreachNoticeRequirement(id: UUID, breachNoticeRequirement: BreachNoticeRequirement): BreachNoticeRequirement {
    val requirementList = requirementRepository.findByBreachNoticeIdAndRequirementId(id, breachNoticeRequirement.requirementId)
    val existingRequirement = if (requirementList.isNotEmpty()) requirementList[0] else null
    val requirement = breachNoticeRequirement.toEntity(existingRequirement)
    return requirementRepository.save(requirement).toModel()
  }

  @Transactional
  fun deleteBreachNoticeContact(id: UUID, contactId: Long) {
    val fetchedContact = contactRepository.findFirstByBreachNoticeIdAndContactId(id, contactId)
    val contactReqLinks = contactRequirementRepository.findByBreachNoticeIdAndContactId(id, fetchedContact.id)
    contactRequirementRepository.deleteAll(contactReqLinks)
    contactRepository.deleteById(fetchedContact.id)
  }

  @Transactional
  fun deleteUnlinkedRequirements(id: UUID) {
    // Find any unlinked requirements and delete
    val breachNoticeRequirements = breachNoticeRepository.findById(id).get().breachNoticeRequirementList.map { r -> r.id }
    val remainingContactReqLinks = contactRequirementRepository.findByBreachNoticeId(id).map { cr -> cr.requirementId }
    val requirementsToDelete = breachNoticeRequirements.filter { it !in remainingContactReqLinks }
    requirementRepository.deleteAllByIdInBatch(requirementsToDelete)
    recalculateRequirementDates(id)
  }

  fun findAllLinksForBreachNoticeWithContactId(breachNoticeId: UUID, contactId: UUID): List<ContactRequirement> = contactRequirementRepository.findByBreachNoticeIdAndContactId(breachNoticeId, contactId).map { cr -> cr.toModel() }

  fun findAllLinksForBreachNotice(breachNoticeId: UUID): List<ContactRequirement> = contactRequirementRepository.findByBreachNoticeId(breachNoticeId).map { cr -> cr.toModel() }

  fun updateContactRequirementLinksForBreachNotice(breachNoticeId: UUID, contactId: UUID, contactRequirements: List<ContactRequirement>) {
    // Grab links from DB
    val existingRecords = contactRequirementRepository.findByBreachNoticeIdAndContactId(breachNoticeId, contactId)
    val existingRequirementLinks = existingRecords.map { cr -> cr.requirementId }
    val newRequirementLinks = contactRequirements.map { cr -> cr.requirementId }
    val recordsToRemove = existingRecords.filter { cr -> !newRequirementLinks.contains(cr.requirementId) }
    contactRequirementRepository.deleteAll(recordsToRemove)
    // Remove any links not present anymore
    val recordsToAdd = contactRequirements.filter { cr -> !existingRequirementLinks.contains(cr.requirementId) }.map { cr -> cr.toEntity() }
    // Add new links
    contactRequirementRepository.saveAll(recordsToAdd)
    recalculateRequirementDates(breachNoticeId)
  }

  private fun findBreachNoticeEntity(id: UUID): BreachNoticeEntity = breachNoticeRepository.findByIdOrNull(id) ?: throw NotFoundException("BreachNoticeEntity", "id", id)

  private fun recalculateRequirementDates(breachNoticeId: UUID) {
    val existingRecords = requirementRepository.findByBreachNoticeId(breachNoticeId)
    for (requirement in existingRecords) {
      val linkedContacts =
        contactRequirementRepository.findByBreachNoticeIdAndRequirementId(breachNoticeId, requirement.id)
          .mapNotNull { cr -> cr.contact }.distinct()
      val dateList: List<LocalDateTime> = linkedContacts.map { contact -> contact.contactDate!! }
      val maxDate = dateList.maxOrNull()
      val minDate = dateList.minOrNull()
      requirement.toDate = maxDate
      requirement.fromDate = minDate
      requirementRepository.save(requirement)
    }
  }

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
    optionalNumber = optionalNumber,
    optionalNumberChecked = optionalNumberChecked,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    conditionBeingEnforced = conditionBeingEnforced,
    selectNextAppointment = selectNextAppointment,
    furtherReasonDetails = furtherReasonDetails,
    breachNoticeContactList = breachNoticeContactList.map {
      it.toEntity(
        existingEntity.breachNoticeContactList.find { existingContactEntity ->
          existingContactEntity.id == it.id
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
    selectNextAppointment = selectNextAppointment,
    furtherReasonDetails = furtherReasonDetails,
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
    selectNextAppointment = selectNextAppointment,
    conditionBeingEnforced = conditionBeingEnforced,
    furtherReasonDetails = furtherReasonDetails,
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
      selectNextAppointment = it.selectNextAppointment,
      reviewEvent = it.reviewEvent,
      conditionBeingEnforced = it.conditionBeingEnforced,
      furtherReasonDetails = it.furtherReasonDetails,
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
    breachNoticeId = breachNoticeId,
  ) ?: BreachNoticeContactEntity(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
    breachNoticeId = breachNoticeId,
  )

  private fun BreachNoticeContactEntity.toModel() = BreachNoticeContact(
    id = id,
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
    breachNoticeId = breachNoticeId,
  )

  private fun BreachNoticeRequirementEntity.toModel() = BreachNoticeRequirement(
    id = id,
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
    breachNoticeId = breachNoticeId,
  )

  private fun BreachNoticeRequirement.toEntity(existingEntity: BreachNoticeRequirementEntity? = null) = existingEntity?.copy(
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
    breachNoticeId = breachNoticeId,
  ) ?: BreachNoticeRequirementEntity(
    requirementId = requirementId,
    requirementTypeMainCategoryDescription = requirementTypeMainCategoryDescription,
    requirementTypeSubCategoryDescription = requirementTypeSubCategoryDescription,
    rejectionReason = rejectionReason,
    fromDate = fromDate,
    toDate = toDate,
    breachNoticeId = breachNoticeId,
  )

  private fun ContactRequirement.toEntity(existingEntity: ContactRequirementEntity? = null) = existingEntity?.copy(
    requirementId = requirementId,
    breachNoticeId = breachNoticeId,
    contactId = contactId,
  ) ?: ContactRequirementEntity(
    requirementId = requirementId,
    breachNoticeId = breachNoticeId,
    contactId = contactId,
  )

  private fun ContactRequirementEntity.toModel() = ContactRequirement(
    requirementId = requirementId,
    breachNoticeId = breachNoticeId,
    contactId = contactId,
    contact = contact?.toModel(),
    requirement = requirement?.toModel(),
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

  fun updateReviewEvent(eventType: ReviewEventType, breachNotice: BreachNoticeEntity, occurredAt: ZonedDateTime) {
    breachNotice.reviewEvent = eventType.name
    breachNotice.reviewRequiredDate = occurredAt.toLocalDateTime()
    breachNoticeRepository.save(breachNotice)
  }

  fun deleteAllByCrn(crn: String) {
    breachNoticeRepository.deleteByCrn(crn)
  }

  fun getAllForSARByCRN(crn: String, fromDate: LocalDate?, toDate: LocalDate?): Collection<BreachNotice> {
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

    // Clear Identifiable user information from the breach notice for SAR
    sarData.forEach { breachNoticeEntity ->
      breachNoticeEntity.titleAndFullName = null
      breachNoticeEntity.optionalNumber = null
      breachNoticeEntity.contactNumber = null
      breachNoticeEntity.responsibleOfficer = null
      breachNoticeEntity.nextAppointmentOfficer = null
    }
    return sarData.map { it.toModel() }
  }
}
