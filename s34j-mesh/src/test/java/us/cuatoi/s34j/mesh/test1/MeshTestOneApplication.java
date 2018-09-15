package us.cuatoi.s34j.mesh.test1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;
import us.cuatoi.s34j.service.mesh.MeshServiceHandler;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestOneApplication {

    @MeshServiceHandler("hello-1")
    public String hello(String name) {
        return "Hello " + name + " from 1";
    }

    public static void main(String[] args) {
        System.setProperty("server.port", "9001");
        SpringApplication.run(MeshTestOneApplication.class, args);
    }

}
