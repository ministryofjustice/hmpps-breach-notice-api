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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.util.Matrix
import java.io.ByteArrayOutputStream
import kotlin.math.cos
import kotlin.math.sin

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

  fun addWatermark(pdfBytes: ByteArray?): ByteArray {
    PDDocument.load(pdfBytes).use { document ->
      val numberOfPages = document.numberOfPages
      for (i in 0 until numberOfPages) {
        val page = document.getPage(i)
        val mediaBox = page.mediaBox ?: PDRectangle.A4

        PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { contentStream ->
          val gs = PDExtendedGraphicsState().apply {
            strokingAlphaConstant = 0.2f
            nonStrokingAlphaConstant = 0.2f
          }

          contentStream.setGraphicsStateParameters(gs)
          contentStream.setNonStrokingColor(0f, 0f, 0f)

          val font = PDType1Font.HELVETICA_BOLD
          val fontSize = 100f
          contentStream.setFont(font, fontSize)

          val centerX = mediaBox.width / 3
          val centerY = mediaBox.height / 3

          val angle = Math.toRadians(45.0)
          val cosA = cos(angle)
          val sinA = sin(angle)

          contentStream.beginText()

          //contentStream.setTextMatrix(cosA.toFloat(), sinA.toFloat(), -sinA.toFloat(), cosA.toFloat(), centerX, centerY)
          var matrix = Matrix(cosA.toFloat(), sinA.toFloat(), -sinA.toFloat(), cosA.toFloat(), centerX, centerY)
          contentStream.setTextMatrix(matrix)

          contentStream.showText("DRAFT")
          contentStream.endText()
        }
      }

      val out = ByteArrayOutputStream()
      document.save(out)
      return out.toByteArray()
    }
  }
}
