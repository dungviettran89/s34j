package us.cuatoi.s34j.sbs.core;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("us.cuatoi.s34j.sbs.core")
@ComponentScan("us.cuatoi.s34j.sbs.core")
class StorageConfig {
}
