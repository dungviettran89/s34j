package us.cuatoi.s34jserver.core.dto.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class BucketPolicyStatementJson {
    @SerializedName("Sid")
    private String sid;
    @SerializedName("Effect")
    private String effect;
    @SerializedName("Principal")
    private BucketPolicyPrincipalJson principal;
    @SerializedName("Action")
    private List<String> action;
    @SerializedName("Resource")
    private List<String> resource;
    @SerializedName("Condition")
    private Map<String, Map<String, List<String>>> condition;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public BucketPolicyPrincipalJson getPrincipal() {
        return principal;
    }

    public void setPrincipal(BucketPolicyPrincipalJson principal) {
        this.principal = principal;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public List<String> getResource() {
        return resource;
    }

    public void setResource(List<String> resource) {
        this.resource = resource;
    }

    public Map<String, Map<String, List<String>>> getCondition() {
        return condition;
    }

    public void setCondition(Map<String, Map<String, List<String>>> condition) {
        this.condition = condition;
    }
}
