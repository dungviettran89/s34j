package us.cuatoi.s34jserver.core.operation.object;

import org.junit.Test;
import us.cuatoi.s34jserver.core.handler.object.PostPolicy;

import static org.junit.Assert.assertNotNull;

public class PostPolicyTest {
    @Test
    public void testParseString() throws Exception {
        String json = "{ \"expiration\": \"2007-12-01T12:00:00.000Z\",\n" +
                "  \"conditions\": [\n" +
                "    {\"acl\": \"public-read\" },\n" +
                "    {\"bucket\": \"johnsmith\" },\n" +
                "    [\"starts-with\", \"$key\", \"user/eric/\"],\n" +
                "  ]\n" +
                "}";
        PostPolicy postPolicy = PostPolicy.parse(json);
        assertNotNull("Must be able to parse", postPolicy);
    }
}
