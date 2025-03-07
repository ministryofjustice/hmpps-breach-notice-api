package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthorisationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /breach-notice/{parameter}")
  inner class BreachNoticeTestEntityEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/breach-notice/{parameter}", "b9a037f2-a558-497b-9fb3-840572e3a17d")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/breach-notice/{parameter}", "b9a037f2-a558-497b-9fb3-840572e3a17d")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }
  }
}
