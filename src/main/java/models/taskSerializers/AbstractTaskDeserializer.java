package models.taskSerializers;

import com.google.gson.*;
import models.tasks.AbstractTask;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AbstractTaskDeserializer implements JsonDeserializer<AbstractTask> {
    private final String abstractTaskTypeElementName;
    private final Gson gson;
    private final Map<String, Class<? extends AbstractTask>> abstractTaskTypeRegistry;

    public AbstractTaskDeserializer(String abstractTypeTypeElementName) {
        this.abstractTaskTypeElementName = abstractTypeTypeElementName;
        this.gson = new Gson();
        this.abstractTaskTypeRegistry = new HashMap<>();
    }

    public void registerBarnType(String abstractTaskTypeName, Class<? extends AbstractTask> abstractType) {
        abstractTaskTypeRegistry.put(abstractTaskTypeName, abstractType);
    }

    @Override
    public AbstractTask deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject abstractTaskObject = json.getAsJsonObject();
        JsonElement abstractTaskTypeElement = abstractTaskObject.get(abstractTaskTypeElementName);

        Class<? extends AbstractTask> abstractTaskType = abstractTaskTypeRegistry.get(abstractTaskTypeElement.getAsString());
        return gson.fromJson(abstractTaskObject, abstractTaskType);
    }

}
