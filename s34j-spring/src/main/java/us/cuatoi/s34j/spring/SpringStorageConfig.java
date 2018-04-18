package us.cuatoi.s34j.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PreDestroy;
import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@Configuration
@EntityScan("us.cuatoi.s34j.spring")
@ComponentScan("us.cuatoi.s34j.spring")
@EnableScheduling
class SpringStorageConfig {
    public static final Logger logger = LoggerFactory.getLogger(SpringStorageConfig.class);
    private final File tempUploadDir;

    public SpringStorageConfig() throws IOException {
        tempUploadDir = Files.createTempDirectory("upload-").toFile();
        logger.info("SpringStorageConfig() tempUploadDir=" + tempUploadDir);
    }

    @Bean
    public SpringStorageServlet springStorageServlet() {
        return new SpringStorageServlet();
    }

    @Bean
    @Autowired
    public ServletRegistrationBean springStorageServletRegistration(SpringStorageServlet servlet) {
        MultipartConfigElement config = new MultipartConfigElement(tempUploadDir.toString());
        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setLoadOnStartup(1);
        registration.setServlet(servlet);
        registration.setMultipartConfig(config);
        registration.setUrlMappings(Collections.singletonList("/*"));
        return registration;
    }

    @PreDestroy
    void stop() {
        logger.info("stop() tempUploadDir=" + tempUploadDir);
        boolean deleteResult = FileSystemUtils.deleteRecursively(tempUploadDir);
        logger.info("stop() deleteResult=" + deleteResult);
    }
}
