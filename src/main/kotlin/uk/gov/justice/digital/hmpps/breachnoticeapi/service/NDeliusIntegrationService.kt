package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class NDeliusIntegrationService(
  private val webClient: WebClient,
  @Value("\${ndelius-integration-api.url}") val ndeliusIntegrationApiUrl: String,
) {
  fun getCrnForBreachNoticeUuid(breachNoticeId: String): NDeliusCrn? = webClient.get()
    .uri(ndeliusIntegrationApiUrl + "/case/{breachNoticeId}", breachNoticeId)
    .retrieve()
    .bodyToMono(NDeliusCrn::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()
}

data class NDeliusCrn(
  val crn: String,
)
