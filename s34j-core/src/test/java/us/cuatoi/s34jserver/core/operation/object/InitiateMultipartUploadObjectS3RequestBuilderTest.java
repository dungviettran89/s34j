package us.cuatoi.s34jserver.core.operation.object;

import org.junit.Test;
import us.cuatoi.s34jserver.core.handler.object.MultipartUploadHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class InitiateMultipartUploadObjectS3RequestBuilderTest {
    @Test
    public void testGenerateId() throws Exception {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            Thread.sleep(2);
            String id = MultipartUploadHandler.generateNewId();
            ids.add(id);
            System.out.println(id);
        }
        for (int i = 0; i < ids.size() - 1; i++) {
            assertTrue(ids.get(i).compareTo(ids.get(i + 1)) < 0);
        }
    }
}
