package us.cuatoi.s34j.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SpringStorageJpaConfig.class, SpringStorageMongoConfig.class, SpringStorageConfig.class})
public @interface EnableSpringStorageService {
}
