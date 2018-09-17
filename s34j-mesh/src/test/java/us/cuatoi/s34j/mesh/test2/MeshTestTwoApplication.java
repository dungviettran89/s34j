package us.cuatoi.s34j.mesh.test2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;
import us.cuatoi.s34j.service.mesh.MeshServiceHandler;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestTwoApplication {

    @MeshServiceHandler("hello-2")
    public String hello(String name) {
        return "Hello " + name + " from 2";
    }

    @MeshServiceHandler("hello-2-error")
    public String helloError(String name) {
        throw new RuntimeException("Error");
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "9002");
        System.setProperty("s34j.service-mesh.node.url", "http://localhost:9002");
        System.setProperty("s34j.service-mesh.node.name", "test-2");
        SpringApplication.run(MeshTestTwoApplication.class, args);
    }

}
