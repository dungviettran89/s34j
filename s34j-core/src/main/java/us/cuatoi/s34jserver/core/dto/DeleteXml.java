package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class DeleteXml extends AbstractXml {
    @Key("Quiet")
    private boolean quiet;
    @Key("Object")
    private List<DeletedXml> objects =new ArrayList<>();

    public DeleteXml() {
        super.namespaceDictionary.set("", "http://s3.amazonaws.com/doc/2006-03-01/");
        super.name = "Delete";
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public List<DeletedXml> getObjects() {
        return objects;
    }

    public void setObjects(List<DeletedXml> objects) {
        this.objects = objects;
    }
}
