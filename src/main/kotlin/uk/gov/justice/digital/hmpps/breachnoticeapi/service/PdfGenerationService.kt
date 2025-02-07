package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.*
import java.nio.charset.StandardCharsets

@Service
class PdfGenerationService(
  private val templateEngine: SpringTemplateEngine,
  private val gotenbergApiClient: GotenbergApiClient,
) {

  fun generateHtml(breachNoticeDetails: BreachNoticeDetails?): String? {
    val context = Context()
    context.setVariable("breachNotice", breachNoticeDetails)

    return templateEngine.process("NAT_Breach_Template", context)
  }

  fun generatePdf(html : String?) : ByteArray? {
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA

    val body = LinkedMultiValueMap<String, Any>()
    body.add(
      "files",
      HttpEntity(
        html?.toByteArray(StandardCharsets.UTF_8),
        HttpHeaders().apply {
          contentType = MediaType.TEXT_HTML
          setContentDispositionFormData("files", "index.html")
        },))
    body.add("paperWidth", "8.27")
    body.add("paperHeight", "11.69")
    body.add("marginTop", 1)
    body.add("marginBottom", 1)
    body.add("marginLeft", 1)
    body.add("marginRight", 1)

    val requestEntity = HttpEntity(body, headers)

    return gotenbergApiClient.convertHtmlToPdf(requestEntity)
  }
}
