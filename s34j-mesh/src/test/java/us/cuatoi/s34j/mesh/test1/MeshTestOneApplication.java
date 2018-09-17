package us.cuatoi.s34j.mesh.test1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import us.cuatoi.s34j.service.mesh.*;
import us.cuatoi.s34j.service.mesh.bo.Invoke;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static us.cuatoi.s34j.service.mesh.MeshFilter.SM_FORWARD_INVOKE;

@SpringBootApplication
@EnableServiceMesh
@EnableScheduling
@Slf4j
public class MeshTestOneApplication {

    @Autowired
    private MeshManager meshManager;
    @Autowired
    private MeshInvoker meshInvoker;
    @Autowired
    private MeshTemplate meshTemplate;

    @MeshServiceHandler("hello-1")
    public String hello(String name) {
        return "Hello " + name + " from 1";
    }


    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 10 * 1000)
    public void printInfo() throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("Invoke 1: {}", meshInvoker.invoke("hello-1", "Test", String.class).get());

        log.info("Invoke 2: {}", meshInvoker.invoke("hello-2", "Test", String.class).get());

        try {
            log.info("Invoke 2 with error: {}", meshInvoker.invoke("hello-2-error", "Test", String.class).get());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Invoke forwardInvoke = new Invoke();
        forwardInvoke.setService("hello-2");
        forwardInvoke.setInputJson("\" forward invoke \"");
        forwardInvoke.setChain(Lists.newArrayList("hello-1"));
        forwardInvoke.setHeaders(new HashMap<>());
        forwardInvoke.setCorrelationId(UUID.randomUUID().toString());
        log.info("Invoke 3: {}", meshTemplate.post("http://localhost:9003", SM_FORWARD_INVOKE, forwardInvoke, Invoke.class));

        ObjectMapper debugMapper = new ObjectMapper();
        debugMapper.enable(SerializationFeature.INDENT_OUTPUT);
        log.info("Mesh: {}", debugMapper.writeValueAsString(meshManager.getMesh()));
        log.info("Latencies: {}", debugMapper.writeValueAsString(meshManager.getExchangeLatencies()));
        log.info("KnownLatencies: {}", debugMapper.writeValueAsString(meshManager.getKnownLatencies()));
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "9001");
        System.setProperty("s34j.service-mesh.node.url", "http://localhost:9001");
        System.setProperty("s34j.service-mesh.node.name", "test-1");
        SpringApplication.run(MeshTestOneApplication.class, args);
    }

}
