package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  private val inboundTopic by lazy { hmppsQueueService.findByTopicId("hmppsbreachnoticetopic") ?: throw MissingQueueException("HmppsTopic inboundtopic not found") }
  protected val inboundSnsClient by lazy { inboundTopic.snsClient }

  private val outboundTopic by lazy { hmppsQueueService.findByTopicId("hmppsbreachnoticepublishtopic") ?: throw MissingQueueException("HmppsTopic hmppsbreachnoticepublishtopic not found") }
  protected val outboundSnsClient by lazy { outboundTopic.snsClient }

  private val outboundQueue by lazy { hmppsQueueService.findByQueueId("hmppsbreachnoticepublishqueue") ?: throw MissingQueueException("HmppsQueue outboundqueue not found") }
  protected val outboundQueueClient by lazy { outboundQueue.sqsClient }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)
}
