package uk.gov.justice.digital.hmpps.breachnoticeapi.model

import jakarta.validation.constraints.Pattern

data class InitialiseBreachNotice(
  @field:Pattern(regexp = "^[A-Z][0-9]{6}")
  val crn: String,
)
