package us.cuatoi.s34j.sbs.core.operation;

import java.io.InputStream;

public class SaveBlockOperation {

    private final String key;
    private final InputStream stream;

    public SaveBlockOperation(String key, InputStream stream) {
        this.key = key;
        this.stream = stream;
    }

    public void execute() {

    }
}
