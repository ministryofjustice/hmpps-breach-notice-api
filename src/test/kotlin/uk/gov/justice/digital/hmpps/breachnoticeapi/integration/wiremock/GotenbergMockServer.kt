package uk.gov.justice.digital.hmpps.breachnoticeapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Paths

class GotenbernApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val gotenberg = GotenbergMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    gotenberg.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    gotenberg.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    gotenberg.stop()
  }
}

class GotenbergMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8072
  }

  fun stubGeneratePdf() {
    val filePath = Paths.get("src/test/resources/test.pdf")
    val fileBytes: ByteArray = Files.readAllBytes(filePath)

    stubFor(
      post(urlEqualTo("/forms/chromium/convert/html"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/pdf")))
            .withBody(
              fileBytes,
            ),
        ),
    )
  }
}
