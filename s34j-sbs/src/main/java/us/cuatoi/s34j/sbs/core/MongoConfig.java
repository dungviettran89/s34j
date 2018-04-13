package us.cuatoi.s34j.sbs.core;

import com.mongodb.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.URI;

@Configuration
@ConditionalOnClass(EnableMongoRepositories.class)
@EnableMongoRepositories
class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public LockProvider lockProvider(MongoClient mongo) {
        String databaseName = URI.create(mongoUri).getPath();
        return new MongoLockProvider(mongo, databaseName);
    }
}
