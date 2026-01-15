package uk.gov.justice.digital.hmpps.breachnoticeapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.breachnoticeapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.InitialiseBreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.BreachNoticeService
import uk.gov.justice.digital.hmpps.breachnoticeapi.service.SnsService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_BREACH_NOTICE')")
@RequestMapping(value = ["/breach-notice"], produces = ["application/json"])
class BreachNoticeController(
  private val breachNoticeService: BreachNoticeService,
  val sqsService: SnsService,
) {
  @GetMapping("/{uuid}")
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
  @Operation(
    summary = "Initialises a Breach Notice",
    description = "Calls through the breach notice service to initialise a breach notice",
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
  fun initialiseBreachNotice(@Valid @RequestBody initialiseBreachNotice: InitialiseBreachNotice) = breachNoticeService.createBreachNotice(initialiseBreachNotice)

  @PutMapping("/{id}")
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
  fun updateBreachNotice(@PathVariable id: UUID, @RequestBody breachNotice: BreachNotice) {
    val original = breachNoticeService.getBreachNoticeById(id)
    breachNoticeService.updateBreachNotice(id, breachNotice)

    if (original != null && original.completedDate == null && breachNotice.completedDate != null) {
      sqsService.sendPublishDomainEvent(breachNotice, id)
    }
  }

  @GetMapping("/{uuid}/pdf")
  @Operation(
    summary = "Retrieve a breach notice pdf by uuid - breach notice id",
    description = "Calls through the breach notice service to retrieve a generate ",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "breach notice pdf returned"),
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
  fun getBreachNoticeAsPdf(@PathVariable uuid: UUID): ResponseEntity<ByteArray> {
    val breachNotice =
      breachNoticeService.getBreachNoticeById(uuid) ?: throw NotFoundException("Breach notice", "id", uuid)
    val pdfBytes = breachNoticeService.getBreachNoticeAsPdf(uuid, breachNotice, breachNotice.completedDate == null)
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_PDF
    headers.contentDisposition = ContentDisposition.attachment()
      .filename("Breach_Notice_" + breachNotice.crn + "_" + breachNotice.referenceNumber + ".pdf").build()
    return ResponseEntity.ok().headers(headers).body(pdfBytes)
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Delete a Breach Notice",
    description = "Calls through the breach notice service to delete a breach notice",
    security = [SecurityRequirement(name = "breach-notice-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Breach Notice deleted"),
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
  fun deleteBreachNotice(@PathVariable id: UUID) {
    val breachNotice = breachNoticeService.getBreachNoticeById(id) ?: throw NotFoundException("Breach notice", "id", id)
    breachNoticeService.deleteBreachNotice(id)
    sqsService.sendDeletedDomainEvent(breachNotice, id)
  }
}
