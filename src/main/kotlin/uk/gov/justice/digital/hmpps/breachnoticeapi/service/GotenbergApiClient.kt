package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient

@Component
class GotenbergApiClient(@Qualifier("gotenbergClient") val gotenbergClient: WebClient) {
  fun convertHtmlToPdf(requestEntity: HttpEntity<LinkedMultiValueMap<String, Any>>) : ByteArray? {
    val gotenbergApiResponse = gotenbergClient
      .post()
      .uri("/forms/chromium/convert/html")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .bodyValue(requestEntity.body ?: LinkedMultiValueMap<String, Any>())
      .retrieve().bodyToMono(ByteArray::class.java)
      .block()
    return gotenbergApiResponse
  }
}