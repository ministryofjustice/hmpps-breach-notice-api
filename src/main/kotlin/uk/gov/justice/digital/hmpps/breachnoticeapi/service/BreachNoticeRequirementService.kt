package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeRequirementEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRequirementRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.RequirementRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
class BreachNoticeRequirementService(
  val breachNoticeRepository: BreachNoticeRepository,
  val contactRequirementRepository: ContactRequirementRepository,
  private val requirementRepository: RequirementRepository,
) {

  fun createBreachNoticeRequirement(breachNoticeRequirement: BreachNoticeRequirement): UUID {
    val id = requirementRepository.save(breachNoticeRequirement.toEntity()).id
    return id
  }

  @Transactional
  fun updateBreachNoticeRequirement(breachNoticeRequirement: BreachNoticeRequirement) {
    val requirementList = requirementRepository.findByBreachNoticeIdAndRequirementId(
      breachNoticeRequirement.breachNoticeId,
      breachNoticeRequirement.requirementId,
    )
    val existingRequirement = if (requirementList.isNotEmpty()) requirementList[0] else null
    val requirement = breachNoticeRequirement.toEntity(existingRequirement)
    requirementRepository.save(requirement).toModel()
  }

  @Transactional
  fun deleteUnlinkedRequirements(id: UUID) {
    // Find any unlinked requirements and delete
    val breachNoticeRequirements =
      breachNoticeRepository.findById(id).get().breachNoticeRequirementList.map { r -> r.id }
    val remainingContactReqLinks = contactRequirementRepository.findByBreachNoticeId(id).map { cr -> cr.requirementId }
    val requirementsToDelete = breachNoticeRequirements.filter { it !in remainingContactReqLinks }
    requirementRepository.deleteAllByIdInBatch(requirementsToDelete)
  }

  fun recalculateRequirementDates(breachNoticeId: UUID) {
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
}
