package uk.gov.justice.digital.hmpps.breachnoticeapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GotenbergConfig (@Value("\${gotenberg.url}") val gotenbergUrl: String,) {
//  @Bean
//  fun templateEngine(): SpringTemplateEngine {
//    val templateEngine = SpringTemplateEngine()
//    val resolver = StringTemplateResolver()
//    resolver.setTemplateMode("HTML")
//    templateEngine.setTemplateResolver(resolver)
//    return templateEngine
//  }

  @Bean
  fun gotenbergClient(): WebClient = WebClient
    .builder()
    .baseUrl(gotenbergUrl)
    .exchangeStrategies(
      ExchangeStrategies
        .builder()
        .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
        .build(),
      ).build()
}