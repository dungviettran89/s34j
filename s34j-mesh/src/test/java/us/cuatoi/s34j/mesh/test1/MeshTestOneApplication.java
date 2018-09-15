package us.cuatoi.s34j.mesh.test1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestOneApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "9001");
        SpringApplication.run(MeshTestOneApplication.class, args);
    }

}
