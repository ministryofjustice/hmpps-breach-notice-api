package uk.gov.justice.digital.hmpps.breachnoticeapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeContact
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeContactService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/contacts"], produces = ["application/json"])
class BreachNoticeContactsController(
  private val breachNoticeContactService: BreachNoticeContactService,
) {

  @PutMapping("/{id}")
  @Operation(
    summary = "Update more that one contact in a single transaction",
    description = "Calls through the breach notice service to update multiple breach notice contacts in a single transaction",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice Contacts updated"),
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
  fun updateBreachNoticeContacts(@PathVariable id: UUID, @RequestBody breachNoticeContacts: List<BreachNoticeContact>) = breachNoticeContactService.batchUpdateBreachNoticeContacts(id, breachNoticeContacts)
}
