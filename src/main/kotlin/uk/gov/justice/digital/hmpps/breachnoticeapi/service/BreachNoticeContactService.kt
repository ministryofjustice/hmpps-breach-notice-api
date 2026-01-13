package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.breachnoticeapi.entity.BreachNoticeContactEntity
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.ContactRequirementRepository
import java.util.UUID

@Service
class BreachNoticeContactService(
  val contactRequirementRepository: ContactRequirementRepository,
  val contactRepository: ContactRepository,
) {

  fun createBreachNoticeContact(breachNoticeContact: BreachNoticeContact) {
    val contact = breachNoticeContact.toEntity()
    contactRepository.save(contact)
  }

  @Transactional
  fun updateBreachNoticeContact(id: UUID, breachNoticeContact: BreachNoticeContact) {
    // get the existing Breach Notice
    val existingBreachNoticeContactEntity: BreachNoticeContactEntity = contactRepository.findById(id).get()

    // if we previously had No selected for whole sentence
    // and it is now yes. We must delete all contact_requirement links
    // for this form and for this contact
    if(breachNoticeContact.wholeSentence == true && (existingBreachNoticeContactEntity.wholeSentence == null || !existingBreachNoticeContactEntity.wholeSentence)) {
      if(breachNoticeContact.id != null) {
        contactRequirementRepository.deleteByBreachNoticeIdAndContactId(existingBreachNoticeContactEntity.breachNoticeId, breachNoticeContact.id)
      }
    }

    // if we had a whole sentence previously and now its not, delete the rejection reason
    if((breachNoticeContact.wholeSentence == null || !breachNoticeContact.wholeSentence) && existingBreachNoticeContactEntity.wholeSentence == true) {
      breachNoticeContact.rejectionReason = null
    }

    val updatedEntity: BreachNoticeContactEntity = breachNoticeContact.toEntity()
    updatedEntity.createdByUser = existingBreachNoticeContactEntity.createdByUser
    updatedEntity.createdDatetime = existingBreachNoticeContactEntity.createdDatetime
    updatedEntity.lastUpdatedUser = existingBreachNoticeContactEntity.lastUpdatedUser
    updatedEntity.lastUpdatedDatetime = existingBreachNoticeContactEntity.lastUpdatedDatetime
    updatedEntity.id = breachNoticeContact.id!!
    contactRepository.save(updatedEntity)
  }

  fun fetchBreachNoticeContact(id: UUID, contactId: Long): BreachNoticeContact = contactRepository.findFirstByBreachNoticeIdAndContactId(id, contactId).toModel()

  fun fetchBreachNoticeContacts(id: UUID): List<BreachNoticeContact> = contactRepository.findByBreachNoticeId(id).map { it.toModel() }

  @Transactional
  fun deleteBreachNoticeContact(contactId: UUID) {
    val fetchedContact = contactRepository.findById(contactId).get()
    val contactReqLinks =
      contactRequirementRepository.findByBreachNoticeIdAndContactId(fetchedContact.breachNoticeId, fetchedContact.id)
    contactRequirementRepository.deleteAll(contactReqLinks)
    contactRepository.deleteById(contactId)
  }

  private fun BreachNoticeContact.toEntity(existingEntity: BreachNoticeContactEntity? = null) = existingEntity?.copy(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
    breachNoticeId = breachNoticeId,
    wholeSentence = wholeSentence,
    rejectionReason = rejectionReason,
  ) ?: BreachNoticeContactEntity(
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
    breachNoticeId = breachNoticeId,
    wholeSentence = wholeSentence,
    rejectionReason = rejectionReason,
  )

  private fun BreachNoticeContactEntity.toModel() = BreachNoticeContact(
    id = id,
    contactDate = contactDate,
    contactType = contactType,
    contactOutcome = contactOutcome,
    contactId = contactId,
    breachNoticeId = breachNoticeId,
    wholeSentence = wholeSentence,
    rejectionReason = rejectionReason,
  )
}
