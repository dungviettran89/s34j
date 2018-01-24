package us.cuatoi.s34jserver.core.dto.json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BucketPolicyJson {
    @SerializedName("Version")
    private String version;
    @SerializedName("Id")
    private String id;
    @SerializedName("Statement")
    private List<BucketPolicyStatementJson> statements;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<BucketPolicyStatementJson> getStatements() {
        return statements;
    }

    public void setStatements(List<BucketPolicyStatementJson> statements) {
        this.statements = statements;
    }
}
