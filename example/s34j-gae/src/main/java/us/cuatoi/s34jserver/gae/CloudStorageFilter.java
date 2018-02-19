package us.cuatoi.s34jserver.gae;

import us.cuatoi.s34jserver.core.servlet.ServletHandler;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudStorageFilter implements Filter {
    private SimpleStorageContext context;
    private ServletHandler handler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        CloudStorageFileSystem bucket = CloudStorageFileSystem.forBucket("s34j-demo.appspot.com");
//        CloudStoragePath baseDir = bucket.getPath("s3");
        Path baseDir = Paths.get("/tmp/");
        try {
            Files.createDirectories(baseDir.resolve("demo1"));
            Files.write(baseDir.resolve("demo1").resolve("test.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
            Files.createDirectories(baseDir.resolve("demo2"));
            Files.write(baseDir.resolve("demo2").resolve("test.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
            Files.createDirectories(baseDir.resolve("demo3"));
            Files.write(baseDir.resolve("demo3").resolve("test.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
            Files.createDirectories(baseDir.resolve("demo4"));
            Files.write(baseDir.resolve("demo4").resolve("test.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context = new SimpleStorageContext();
        context.setBaseDir(baseDir);
        context.setAccessKey("Q3AM3UQ867SPQQA43P2F");
        context.setSecretKey("zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
        context.setServerId("s34j-gae");
        context.setRegion("us-central-1");
        context.setAdminEnabled(true);
        handler = new ServletHandler(context);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!handler.service((HttpServletRequest) request, (HttpServletResponse) response)) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
