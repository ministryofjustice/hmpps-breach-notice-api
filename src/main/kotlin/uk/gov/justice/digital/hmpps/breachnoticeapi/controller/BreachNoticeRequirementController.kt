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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeRequirement
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeRequirementService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/requirement"], produces = ["application/json"])
class BreachNoticeRequirementController(
  private val breachNoticeRequirementService: BreachNoticeRequirementService,
) {

  @PostMapping
  @Operation(
    summary = "Initialises a Breach Notice Contact",
    description = "Calls through the breach notice service to initialise a breach notice contact",
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
  fun initialiseBreachNoticeRequirement(@Valid @RequestBody breachNoticeRequirement: BreachNoticeRequirement): UUID = breachNoticeRequirementService.createBreachNoticeRequirement(breachNoticeRequirement)

  @PutMapping("/{id}")
  @Operation(
    summary = "Update a Breach Notice Requirement",
    description = "Calls through the breach notice service to add or update a breach notice requirement",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice requirement updated"),
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
        description = "The Requirement id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateBreachNoticeRequirement(
    @PathVariable id: UUID,
    @RequestBody breachNoticeRequirement: BreachNoticeRequirement,
  ) = breachNoticeRequirementService.updateBreachNoticeRequirement(breachNoticeRequirement)

  @DeleteMapping("/unlinkedrequirements/{breachNoticeId}")
  @Operation(
    summary = "Delete a Breach Notice Requirements",
    description = "Calls through the breach notice service to delete any breach notice requirements which are not linked in the contact-requirement table",
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
  fun deleteUnlinkedBreachNoticeRequirements(@PathVariable breachNoticeId: UUID) = breachNoticeRequirementService.deleteUnlinkedRequirements(breachNoticeId)

  @PutMapping("/recalculateFromToDate/{breachNoticeId}")
  @Operation(
    summary = "Recalculates from & To date of requirements",
    description = "Recalculates the from & To date of all requirements for a breach notice",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice requirement dates updated"),
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
  fun updateBreachNoticeRequirementDates(
    @PathVariable breachNoticeId: UUID,
  ) = breachNoticeRequirementService.recalculateRequirementDates(breachNoticeId)
}
