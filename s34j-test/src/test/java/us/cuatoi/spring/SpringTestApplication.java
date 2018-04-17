package us.cuatoi.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34j.sbs.core.EnableSimpleBlockStorage;
import us.cuatoi.s34j.sbs.test.TestConfigurator;

@SpringBootApplication
@EnableSimpleBlockStorage
public class SpringTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

    @Bean
    public TestConfigurator testConfigurator() {
        return new TestConfigurator();
    }

}
