package us.cuatoi.s34j.mesh.test3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;
import us.cuatoi.s34j.service.mesh.MeshServiceHandler;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestThreeApplication {

    @MeshServiceHandler("hello-3")
    public String hello(String name) {
        return "Hello " + name + " from 3";
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "9003");
        System.setProperty("s34j.service-mesh.active", "true");
        System.setProperty("s34j.service-mesh.node.url", "http://localhost:9003");
        System.setProperty("s34j.service-mesh.node.name", "test-3");
        SpringApplication.run(MeshTestThreeApplication.class, args);
    }

}
