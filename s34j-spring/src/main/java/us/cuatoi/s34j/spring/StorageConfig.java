package us.cuatoi.s34j.spring;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EntityScan("us.cuatoi.s34j.sbs.spring")
@ComponentScan("us.cuatoi.s34j.sbs.spring")
@EnableScheduling
class StorageConfig {
}
