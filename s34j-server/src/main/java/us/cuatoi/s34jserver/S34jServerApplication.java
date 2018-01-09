package us.cuatoi.s34jserver;

import com.google.common.collect.Lists;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Servlet;

@SpringBootApplication
public class S34jServerApplication {

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        S3Context s3Context = new S3Context();
        S3Servlet s3Servlet = new S3Servlet(s3Context);

        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setName(S3Servlet.class.getSimpleName());
        registration.setServlet(s3Servlet);
        registration.setLoadOnStartup(1);
        registration.setUrlMappings(Lists.newArrayList("/*"));
        return registration;
    }

    public static void main(String[] args) {
        SpringApplication.run(S34jServerApplication.class, args);
    }
}
