package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeRequirementEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.ContactRequirementEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.ContactRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRequirementRepository
import java.util.UUID

@Service
class ContactRequirementLinksService(
  val contactRequirementRepository: ContactRequirementRepository,
  private val breachNoticeRequirementService: BreachNoticeRequirementService,
) {

  fun createContactRequirementLink(contactRequirement: ContactRequirement) {
    contactRequirementRepository.save(contactRequirement.toEntity())
  }

  @Transactional
  fun deleteContactRequirementLink(contactRequirementId: UUID) {
    contactRequirementRepository.deleteById(contactRequirementId)
  }

  fun findAllLinksForBreachNoticeWithContactId(breachNoticeId: UUID, contactId: UUID): List<ContactRequirement> = contactRequirementRepository.findByBreachNoticeIdAndContactId(breachNoticeId, contactId).map { cr -> cr.toModel() }

  fun findAllLinksForBreachNotice(breachNoticeId: UUID): List<ContactRequirement> = contactRequirementRepository.findByBreachNoticeId(breachNoticeId).map { cr -> cr.toModel() }

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
}
