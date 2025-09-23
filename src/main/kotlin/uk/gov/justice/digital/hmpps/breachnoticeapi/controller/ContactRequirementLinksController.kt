package uk.gov.justice.digital.hmpps.breachnoticeapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.ContactRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.ContactRequirementLinksService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/crlinks"], produces = ["application/json"])
class ContactRequirementLinksController(
  private val contactRequirementLinksService: ContactRequirementLinksService,
) {

  @PostMapping
  @Operation(
    summary = "Creates a Contact-Requirement link",
    description = "Calls through the breach notice service to create a contact-requirement link",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "201", description = "Contact-Requirement link created"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createContactRequirementLink(@Valid @RequestBody contactRequirement: ContactRequirement) = contactRequirementLinksService.createContactRequirementLink(contactRequirement)

  @GetMapping("/bybreachnoticeid/{breachNoticeId}")
  @Operation(
    summary = "Retrieve a links between contacts & requirements for a breach notice",
    description = "Calls through the breach notice service to retrieve a set of object in the contact-requirement table which match the breach notice id",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "linked list returned"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getBreachNoticeContactRequirementLinks(@PathVariable breachNoticeId: UUID): List<ContactRequirement>? = contactRequirementLinksService.findAllLinksForBreachNotice(breachNoticeId)

  @GetMapping("/bybreachnoticeidandcontactid/{breachNoticeId}/{contactId}")
  @Operation(
    summary = "Retrieve a links between contacts & requirements for a breach notice",
    description = "Calls through the breach notice service to retrieve a set of object in the contact-requirement table which match the records breach notice id and contact id",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "linked list returned"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getBreachNoticeContactRequirementLinksWithContactId(
    @PathVariable breachNoticeId: UUID,
    @PathVariable contactId: UUID,
  ): List<ContactRequirement>? = contactRequirementLinksService.findAllLinksForBreachNoticeWithContactId(breachNoticeId, contactId)

  @DeleteMapping("/{contactRequirementId}")
  @Operation(
    summary = "Delete a Contact-Requirement link",
    description = "Calls through the breach notice service to delete a contact_requirement link",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Contact-Requirement link deleted"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Contact-Requirement id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteBreachNoticeContactRequirement(@PathVariable contactRequirementId: UUID) = contactRequirementLinksService.deleteContactRequirementLink(contactRequirementId)
}
