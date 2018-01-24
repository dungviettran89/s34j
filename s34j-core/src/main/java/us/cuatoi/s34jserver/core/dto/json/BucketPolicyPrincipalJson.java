package us.cuatoi.s34jserver.core.dto.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BucketPolicyPrincipalJson {
    @SerializedName("AWS")
    private List<String> aws;
    @SerializedName("CanonicalUser")
    private List<String> canonicalUser;

    public List<String> getAws() {
        return aws;
    }

    public void setAws(List<String> aws) {
        this.aws = aws;
    }

    public List<String> getCanonicalUser() {
        return canonicalUser;
    }

    public void setCanonicalUser(List<String> canonicalUser) {
        this.canonicalUser = canonicalUser;
    }
}
