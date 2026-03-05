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
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeContactService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/contact"], produces = ["application/json"])
class BreachNoticeContactController(
  private val breachNoticeContactService: BreachNoticeContactService,
) {

  @PostMapping
  @Operation(
    summary = "Initialises a Breach Notice Requirement",
    description = "Calls through the breach notice service to initialise a breach notice Requirement",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "201", description = "Breach Notice created"),
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
  fun initialiseBreachNoticeContact(@Valid @RequestBody breachNoticeContact: BreachNoticeContact) = breachNoticeContactService.createBreachNoticeContact(breachNoticeContact)

  @GetMapping("/bybreachnoticeidanddeliusid/{breachNoticeId}/{deliusContactId}")
  @Operation(
    summary = "Retrieve a Breach Notice Contact",
    description = "Calls through the breach notice service to retrieve a breach notice contact using breach notice id and delius contact id",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice Contact returned"),
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
        description = "The Breach Notice id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getBreachNoticeContact(
    @PathVariable breachNoticeId: UUID,
    @PathVariable deliusContactId: Long,
  ): BreachNoticeContact = breachNoticeContactService.fetchBreachNoticeContact(breachNoticeId, deliusContactId)

  @GetMapping("/bybreachnoticeid/{breachNoticeId}")
  @Operation(
    summary = "Retrieve a Breach Notice Contact",
    description = "Calls through the breach notice service to retrieve a list of breach notice contacts using breach notice id",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice Contact returned"),
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
        description = "The Contact id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getBreachNoticeContacts(@PathVariable breachNoticeId: UUID): List<BreachNoticeContact> = breachNoticeContactService.fetchBreachNoticeContacts(breachNoticeId)

  @DeleteMapping("/{contactId}")
  @Operation(
    summary = "Delete a Breach Notice Contact",
    description = "Calls through the breach notice service to delete a breach notice contact and its contact_requirement links",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice Contact returned"),
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
        description = "The Contact id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteBreachNoticeContact(@PathVariable contactId: UUID) = breachNoticeContactService.deleteBreachNoticeContact(contactId)
}
