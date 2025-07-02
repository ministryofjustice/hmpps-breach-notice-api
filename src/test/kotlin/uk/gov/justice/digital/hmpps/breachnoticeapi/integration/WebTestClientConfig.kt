package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@TestConfiguration
class WebTestClientConfig {
  @LocalServerPort
  private var port: Int = 0

  @Bean
  fun webTestClient(): WebTestClient = WebTestClient
    .bindToServer()
    .responseTimeout(Duration.ofSeconds(30))
    .baseUrl("http://localhost:$port")
    .build()
}
