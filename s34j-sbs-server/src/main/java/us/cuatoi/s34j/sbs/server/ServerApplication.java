package us.cuatoi.s34j.sbs.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.sbs.core.EnableSimpleBlockStorage;

@SpringBootApplication
@EnableSimpleBlockStorage
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
