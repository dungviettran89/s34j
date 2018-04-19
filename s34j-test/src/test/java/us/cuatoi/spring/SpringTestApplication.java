package us.cuatoi.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34j.sbs.core.EnableSimpleBlockStorage;
import us.cuatoi.s34j.sbs.test.TestConfigurator;
import us.cuatoi.s34j.spring.EnableSpringStorageService;
import us.cuatoi.s34j.spring.auth.AuthenticationProvider;
import us.cuatoi.s34j.test.TestHelper;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@SpringBootApplication
@EnableSimpleBlockStorage
@EnableSpringStorageService
public class SpringTestApplication {
    @Bean
    public TestConfigurator testConfigurator() {
        return new TestConfigurator();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return (k) -> equalsIgnoreCase(TestHelper.DEFAULT_KEY, k) ?
                TestHelper.DEFAULT_SECRET : null;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

}
