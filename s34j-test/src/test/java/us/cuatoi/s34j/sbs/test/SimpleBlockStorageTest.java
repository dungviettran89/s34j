package us.cuatoi.s34j.sbs.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.cuatoi.s34j.sbs.core.SimpleBlockStorage;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleBlockStorageTest {
    @Autowired
    private SimpleBlockStorage simpleBlockStorage;

    @Test
    public void testSimpleBlockStorageInjected() {
        assertNotNull(simpleBlockStorage);
    }
}
