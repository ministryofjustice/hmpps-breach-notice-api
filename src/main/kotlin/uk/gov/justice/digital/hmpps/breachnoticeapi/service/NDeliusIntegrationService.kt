package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Service
class NDeliusIntegrationService(
  private val webClient: WebClient,
  @Value("\${ndelius-integration-api.url}") val ndeliusIntegrationApiUrl: String,
) {
  fun getTime(): LocalDateTime = LocalDateTime.now()

  fun getCrnForBreachNoticeUuid(breachNoticeId: String): NDeliusCrn? = webClient.get()
    // Note that we don't use string interpolation ("/${parameter}").
    // This is important - using string interpolation causes each uri to be added as a separate path in app
    // insights and you'll run out of memory in your app.
    // Also note that this is just an example and the /example-external-api endpoint doesn't exist in this kotlin
    // template project so will return a not found response each time.
    .uri(ndeliusIntegrationApiUrl + "/case/{breachNoticeId}", breachNoticeId)
    .retrieve()
    .bodyToMono(NDeliusCrn::class.java)
    .block()
}

data class NDeliusCrn(
  val crn: String,
)
