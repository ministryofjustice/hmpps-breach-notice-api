package uk.gov.justice.digital.hmpps.breachnoticeapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmppsBreachNoticeApi

fun main(args: Array<String>) {
  runApplication<HmppsBreachNoticeApi>(*args)
}
