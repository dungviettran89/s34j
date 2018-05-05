package us.cuatoi.s34j.test.pubsub;

import java.util.UUID;

public class TestMessage {
    private long id;
    private String message;
    private int testInt;

    public TestMessage() {
        id = System.currentTimeMillis();
        message = "testMessage-" + UUID.randomUUID().toString();
        testInt = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTestInt() {
        return testInt;
    }

    public void setTestInt(int testInt) {
        this.testInt = testInt;
    }
}
