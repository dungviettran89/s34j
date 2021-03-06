package us.cuatoi.s34j.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34j.spring.auth.AuthenticationProvider;
import us.cuatoi.s34j.test.TestHelper;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@SpringBootApplication
@EnableSpringStorageService
public class SpringTestApplication {

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return (k) -> equalsIgnoreCase(TestHelper.DEFAULT_KEY, k) ?
                TestHelper.DEFAULT_SECRET : null;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

}
