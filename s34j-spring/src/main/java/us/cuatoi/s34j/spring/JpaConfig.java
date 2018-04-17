package us.cuatoi.s34j.spring;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import us.cuatoi.s34j.spring.model.ScheduleLockModel;

import javax.sql.DataSource;


@Configuration
@ConditionalOnClass(EnableJpaRepositories.class)
@EnableJpaRepositories
class JpaConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource, ScheduleLockModel.class.getSimpleName());
    }
}
