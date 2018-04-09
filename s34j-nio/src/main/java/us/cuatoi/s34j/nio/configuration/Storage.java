package us.cuatoi.s34j.nio.configuration;

import java.nio.file.Path;

public class Storage {
    private String name;
    private Path path;

    public String getName() {
        return name;
    }

    public Storage setName(String name) {
        this.name = name;
        return this;
    }

    public Path getPath() {
        return path;
    }

    public Storage setPath(Path path) {
        this.path = path;
        return this;
    }
}
