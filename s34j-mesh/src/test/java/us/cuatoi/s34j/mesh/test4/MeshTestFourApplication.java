package us.cuatoi.s34j.mesh.test4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;
import us.cuatoi.s34j.service.mesh.MeshServiceHandler;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestFourApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "9004");
        System.setProperty("s34j.service-mesh.active", "false");
        System.setProperty("s34j.service-mesh.node.url", "http://localhost:9004");
        System.setProperty("s34j.service-mesh.node.name", "test-4");
        SpringApplication.run(MeshTestFourApplication.class, args);
    }

    @MeshServiceHandler("hello-4")
    public String hello(String name) {
        return "Hello " + name + " from 4";
    }

}
