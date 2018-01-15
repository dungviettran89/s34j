package us.cuatoi.s34jserver.core.operation.object;

import com.google.gson.*;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.S3Exception;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class Policy {

    private static GsonBuilder GSON_BUILDER = new GsonBuilder()
            .registerTypeAdapter(Condition.class, new Parser());

    private String expiration;
    private List<Condition> conditions = new ArrayList<>();

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public static Policy parse(String string) {
        return GSON_BUILDER.create().fromJson(string, Policy.class);
    }

    static class Parser implements JsonDeserializer<Condition> {

        @Override
        public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                for (String key : object.keySet()) {
                    Condition condition = new Condition();
                    condition.setOperator("eq");
                    condition.setField("$" + key);
                    condition.setValue(object.get(key).getAsString());
                    return condition;
                }
            } else if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                try {
                    Condition condition = new Condition();
                    condition.setOperator(array.get(0).getAsString());
                    condition.setField(array.get(0).getAsString());
                    condition.setLowerBound(array.get(1).getAsLong());
                    condition.setUpperBound(array.get(2).getAsLong());
                    return condition;
                } catch (Exception ex) {
                    Condition condition = new Condition();
                    condition.setOperator(array.get(0).getAsString());
                    condition.setField(array.get(1).getAsString());
                    condition.setValue(array.get(2).getAsString());
                    return condition;
                }
            }
            return null;
        }
    }

    static class Condition {
        private String field;
        private String operator;
        private String value;
        private long lowerBound;
        private long upperBound;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public long getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(long lowerBound) {
            this.lowerBound = lowerBound;
        }

        public long getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(long upperBound) {
            this.upperBound = upperBound;
        }
    }
}
