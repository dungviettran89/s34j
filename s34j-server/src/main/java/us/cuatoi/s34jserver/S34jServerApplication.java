package us.cuatoi.s34jserver;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Servlet;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@SpringBootApplication
public class S34jServerApplication {

    @Value("${s34j.path:data}")
    private String path;
    @Value("${s34j.accessKey:Q3AM3UQ867SPQQA43P2F}")
    private String accessKey;
    @Value("${s34j.secretKey:zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG}")
    private String secretKey = "";
    @Value("${s34j.region:us-central-1}")
    private String region;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public ServletRegistrationBean servletRegistrationBean() throws IOException {
        Path basePath = Paths.get(path);
        S3Context s3Context = new S3Context(basePath) {
            @Override
            public String getSecretKey(String accessKey) {
                if (equalsIgnoreCase(accessKey, S34jServerApplication.this.accessKey)) {
                    return S34jServerApplication.this.secretKey;
                } else {
                    return String.valueOf(System.currentTimeMillis());
                }
            }
        }.setRegion(region);
        String uploadDir = Files.createTempDirectory("upload").toAbsolutePath().toString();
        MultipartConfigElement mce = new MultipartConfigElement(uploadDir);
        S3Servlet s3Servlet = new S3Servlet(s3Context);
        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setName(S3Servlet.class.getSimpleName());
        registration.setServlet(s3Servlet);
        registration.setLoadOnStartup(1);
        registration.setMultipartConfig(mce);
        registration.setUrlMappings(Lists.newArrayList("/*"));

        logger.warn("starting S34J server.");
        logger.warn("path=" + basePath.toUri());
        logger.warn("accessKey=" + accessKey);
        logger.warn("secretKey=" + secretKey);
        logger.warn("region=" + region);

        return registration;
    }

    public static void main(String[] args) {
        setFromArgs(args, "s34j.path", 0);
        setFromArgs(args, "s34j.accessKey", 1);
        setFromArgs(args, "s34j.secretKey", 2);
        setFromArgs(args, "s34j.region", 3);
        SpringApplication.run(S34jServerApplication.class, args);
    }

    private static void setFromArgs(String[] args, String key, int index) {
        if (args.length > index) {
            System.setProperty(key, args[index]);
        }
    }
}
