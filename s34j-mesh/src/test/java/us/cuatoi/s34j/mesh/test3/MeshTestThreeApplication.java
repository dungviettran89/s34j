package us.cuatoi.s34j.mesh.test3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestThreeApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "9003");
        SpringApplication.run(MeshTestThreeApplication.class, args);
    }

}
