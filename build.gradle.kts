plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.7"
  kotlin("plugin.spring") version "2.3.10"
  kotlin("plugin.jpa") version "2.3.10"
  id("org.flywaydb.flyway") version "13.2.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.apache.pdfbox:pdfbox:2.0.35")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.6.3")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.38") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
