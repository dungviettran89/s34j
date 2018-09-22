package us.cuatoi.s34j.mesh.test2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;
import us.cuatoi.s34j.service.mesh.MeshInvoker;
import us.cuatoi.s34j.service.mesh.MeshServiceHandler;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
@EnableServiceMesh
@EnableScheduling
@Slf4j
public class MeshTestTwoApplication {

    @MeshServiceHandler("hello-2")
    public String hello(String name) {
        return "Hello " + name + " from 2";
    }

    @MeshServiceHandler("hello-2-error")
    public String helloError(String name) {
        throw new RuntimeException("Error");
    }

    @Autowired
    private MeshInvoker meshInvoker;

    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 10 * 1000)
    public void test() throws ExecutionException, InterruptedException {
        log.info("getNodeInfo {}", meshInvoker.invokeJson("getNodeInfo", "\"test-1\"", "test-1").get());
        log.info("getMeshInfo {}", meshInvoker.invokeJson("getMeshInfo", "\"test\"", "test-2").get());
        log.info("getLatencies {}", meshInvoker.invokeJson("getLatencies", "\"test-3\"", "test-3").get());
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "9002");
        System.setProperty("s34j.service-mesh.node.url", "http://localhost:9002");
        System.setProperty("s34j.service-mesh.node.name", "test-2");
        SpringApplication.run(MeshTestTwoApplication.class, args);
    }

}
