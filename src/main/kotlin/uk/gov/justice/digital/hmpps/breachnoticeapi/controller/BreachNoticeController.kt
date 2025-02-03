package uk.gov.justice.digital.hmpps.breachnoticeapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

@RestController
// Role here is specific to the UI.
@PreAuthorize("hasRole('ROLE_TEMPLATE_KOTLIN__UI')")
@RequestMapping(value = ["/breach-notice"], produces = ["application/json"])
class BreachNoticeController(private val breachNoticeService: BreachNoticeService) {
  @GetMapping("/{uuid}")
  @Tag(name = "Breach Notice")
  @Operation(
    summary = "Retrieve a draft breach notice by uuid - breach notice id",
    description = "Calls through the breach notice service to retrieve breach requests",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "breach notice returned"),
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
  fun getBreachNoticeById(@PathVariable uuid: UUID): BreachNoticeDetails? = breachNoticeService.getBreachNoticeById(uuid)

  @PostMapping
  @Tag(name = "Breach Notice")
  @Operation(
    summary = "Create a Breach Notice",
    description = "Calls through the breach notice service to create a breach notice",
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
  fun createBreachNotice(@Valid @RequestBody breachNotice: BreachNotice) = breachNoticeService.createBreachNotice(breachNotice)


  @PutMapping("/{id}")
  @Tag(name = "Breach Notice")
  @Operation(
    summary = "Update a Breach Notice",
    description = "Calls through the breach notice service to update a breach notice",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice updated"),
      ApiResponse(
        responseCode = "400",
        description = "cant change the CRN on an update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
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
  fun updateBreachNotice(@PathVariable id: UUID, @RequestBody breachNotice: BreachNotice) = breachNoticeService.updateBreachNotice(id, breachNotice)
}
