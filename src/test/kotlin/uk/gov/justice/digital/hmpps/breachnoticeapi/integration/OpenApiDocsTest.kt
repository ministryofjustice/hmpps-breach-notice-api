package uk.gov.justice.digital.hmpps.breachnoticeapi.integration

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType

class OpenApiDocsTest(
  @Autowired private val buildProperties: BuildProperties,
  @LocalServerPort private val port: Int = 0,
) : IntegrationTestBase() {

  @Test
  fun `open api docs are available`() {
    webTestClient.get()
      .uri("/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    webTestClient.get()
      .uri("/swagger-ui.html")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is3xxRedirection
      .expectHeader().value("Location") { it.contains("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config") }
  }

  @Test
  fun `the open api json contains documentation`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("paths").isNotEmpty
  }

  @Test
  fun `the swagger json don't contain any duplicate methods`() {
    // Methods in resource classes with the same name end up with operationIds that have _1 and _2 etc. in the name.
    // When the code is then generated from the api docs we refer to the endpoint by operationId. If a new method is
    // added or one removed then the _1 / _2 etc. can change order and thus we end up calling a completely different
    // endpoint next time the code is generated. This test then prevents that from happening by ensuring all endpoints
    // have method names / operation ids.
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("*..operationId").value<List<String>> { list ->
        assertThat(list).filteredOn { it.contains("_") }.isEmpty()
      }
  }

  @Test
  fun `the open api json contains the version number`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").isEqualTo(buildProperties.version)
  }

  @Test
  fun `the open api json is valid`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
  }

  @Test
  fun `the open api json path security requirements are valid`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)

    val exclusions = setOf(
      "/queue-admin/retry-dlq/{dlqName}",
      "/queue-admin/retry-all-dlqs",
      "/queue-admin/purge-queue/{queueName}",
      "/queue-admin/get-dlq-messages/{dlqName}",
      "/subject-access-request",
    )

    // The security requirements of each path don't appear to be validated like they are at https://editor.swagger.io/
    // We therefore need to grab all the valid security requirements and check that each path only contains those items
    val securityRequirements = result.openAPI.security.flatMap { it.keys }
    result.openAPI.paths.filter { !exclusions.contains(it.key) }.filter { it.value.get != null }.forEach { pathItem ->
      assertThat(pathItem.value.get.security.flatMap { it.keys }).isSubsetOf(securityRequirements)
    }
  }

  @ParameterizedTest
  @CsvSource(value = ["breach-notice-api-ui-role, ROLE_BREACH_NOTICE"])
  fun `the security scheme is setup for bearer tokens`(key: String, role: String) {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.$key.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.$key.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.$key.description").value<String> {
        assertThat(it).contains(role)
      }
      .jsonPath("$.components.securitySchemes.$key.bearerFormat").isEqualTo("JWT")
      .jsonPath("$.security[0].$key").isEqualTo(JSONArray().apply { this.add("read") })
  }

  @Test
  fun `all endpoints have a security scheme defined`() {
    // There are 4 SQS endpoints without security scheme, to be excluded from this test; all these endpoint has single tag "hmpps-queue-resource"
    val queueAdminTag = "hmpps-queue-resource"
    val subjectAccessTag = "Subject Access Request"
    val excludedEndpointCount = 5

    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.paths[*][*][?(!@.security)]..tags[0]").value<JSONArray> {
        assertThat(it).hasSize(excludedEndpointCount)
        it.forEach { tag -> assertThat(tag).isIn(queueAdminTag, subjectAccessTag) }
      }
  }
}
