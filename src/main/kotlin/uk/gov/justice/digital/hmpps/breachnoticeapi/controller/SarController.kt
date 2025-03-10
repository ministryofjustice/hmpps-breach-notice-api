package uk.gov.justice.digital.hmpps.breachnoticeapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/subject-access-request"], produces = ["application/json"])
class SarController(private val breachNoticeService: BreachNoticeService) {

  @GetMapping
  @PreAuthorize("hasRole('ROLE_SAR_DATA_ACCESS')")
  @Operation(
    summary = "API call to retrieve SAR data from a product",
    description = "Calls through the breach notice service to retrieve a any SAR related information ",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Request successfully processed - content found"),
      ApiResponse(responseCode = "204", description = "Request successfully processed - no content found"),
      ApiResponse(responseCode = "209", description = "Subject Identifier is not recognised by this service"),
      ApiResponse(
        responseCode = "400",
        description = "The request was not formed correctly",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSAR(
    @RequestParam prn: String?,
    @RequestParam crn: String?,
    @RequestParam fromDate: String?,
    @RequestParam toDate: String?,
  ) = breachNoticeService.getSARDetails(prn, crn, fromDate, toDate)
}
