package uk.gov.justice.digital.hmpps.breachnoticeapi.advice

import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.breachnoticeapi.exception.NotFoundException

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps"])
class ControllerAdvice {
  @ExceptionHandler(NotFoundException::class)
  fun handleNotFound(e: NotFoundException) = ResponseEntity
    .status(NOT_FOUND)

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDenied(e: AccessDeniedException) = ResponseEntity
    .status(FORBIDDEN)
}
