package uk.gov.justice.digital.hmpps.breachnoticeapi.model

data class Address(
  val buildingName: String? = null,
  val addressNumber: String? = null,
  val streetName: String? = null,
  val district: String? = null,
  val townCity: String? = null,
  val county: String? = null,
  val postcode: String? = null,
)
