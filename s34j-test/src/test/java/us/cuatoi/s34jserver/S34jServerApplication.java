package us.cuatoi.s34jserver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageServlet;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

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
        HashMap<String, String> parameters = Maps.newHashMap();
        parameters.put("region", region);
        parameters.put("secretKey", secretKey);
        parameters.put("accessKey", accessKey);
        parameters.put("path", path);
        String uploadDir = Files.createTempDirectory("upload").toAbsolutePath().toString();
        MultipartConfigElement mce = new MultipartConfigElement(uploadDir);
        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setName(SimpleStorageServlet.class.getSimpleName());
        registration.setServlet(new SimpleStorageServlet());
        registration.setLoadOnStartup(1);
        registration.setMultipartConfig(mce);
        registration.setUrlMappings(Lists.newArrayList("/*"));
        registration.setInitParameters(parameters);

        logger.warn("starting S34J server.");
        logger.warn("path=" + path);
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
