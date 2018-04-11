package us.cuatoi.s34j.sbs.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnClass(EnableJpaRepositories.class)
@EnableJpaRepositories
class JpaConfig {
}
