package uk.gov.justice.digital.hmpps.breachnoticeapi.config

import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Component(value = "auditorAware")
class AuditorAwareImplementation : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(SecurityContextHolder.getContext()?.authentication?.name)
}
