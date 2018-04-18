package us.cuatoi.s34j.sbs.core;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({BlockStorageJpaConfig.class, BlockStorageMongoConfig.class, StorageConfig.class})
public @interface EnableSimpleBlockStorage {
}
