package us.cuatoi.s34jserver.core.operation.object;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class PostPolicy {

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

    public static PostPolicy parse(String string) {
        return GSON_BUILDER.create().fromJson(string, PostPolicy.class);
    }

    static class Parser implements JsonDeserializer<Condition> {

        @Override
        public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                for (String key : object.keySet()) {
                    return new Condition()
                            .setOperator("eq")
                            .setField("$" + key)
                            .setValue(object.get(key).getAsString());
                }
            } else if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                try {
                    return new Condition()
                            .setOperator(array.get(0).getAsString())
                            .setField(array.get(0).getAsString())
                            .setLowerBound(array.get(1).getAsLong())
                            .setUpperBound(array.get(2).getAsLong());
                } catch (Exception ex) {
                    return new Condition()
                            .setOperator(array.get(0).getAsString())
                            .setField(array.get(1).getAsString())
                            .setValue(array.get(2).getAsString());
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

        public Condition setField(String field) {
            this.field = field;
            return this;
        }

        public String getOperator() {
            return operator;
        }

        public Condition setOperator(String operator) {
            this.operator = operator;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Condition setValue(String value) {
            this.value = value;
            return this;
        }

        public long getLowerBound() {
            return lowerBound;
        }

        public Condition setLowerBound(long lowerBound) {
            this.lowerBound = lowerBound;
            return this;
        }

        public long getUpperBound() {
            return upperBound;
        }

        public Condition setUpperBound(long upperBound) {
            this.upperBound = upperBound;
            return this;
        }
    }
}
