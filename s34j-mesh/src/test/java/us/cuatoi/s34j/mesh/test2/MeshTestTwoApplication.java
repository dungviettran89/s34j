package us.cuatoi.s34j.mesh.test2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.cuatoi.s34j.service.mesh.EnableServiceMesh;

@SpringBootApplication
@EnableServiceMesh
public class MeshTestTwoApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "9002");
        SpringApplication.run(MeshTestTwoApplication.class, args);
    }

}
