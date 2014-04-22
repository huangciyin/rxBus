package me.streamis.rxbus;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
class JsonParser {

  private static final ObjectMapper mapper = new ObjectMapper();

  static final String MSG_TYPE = "__MSG_TYPE__";
  static final String FAILED = "__FAILED__";

  static {
    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
  }

  static <T> JsonElement asJson(T obj) throws Exception {
    JsonElement json;
    if (obj instanceof Sendable) {
      json = new JsonObject(mapper.writeValueAsString(obj));
      MessageType messageType = obj.getClass().getAnnotation(MessageType.class);
      if (messageType != null && messageType.value() != null) {
        ((JsonObject) json).putString(MSG_TYPE, messageType.value());
      }
    } else if (obj instanceof Collection) {
      json = new JsonArray();
      JsonArray jsonArray = json.asArray();
      for (Object element : (Collection) obj) jsonArray.add(asJson(element));
    } else if (obj instanceof Map) {
      json = new JsonObject((Map) obj);
    } else {
      throw new IllegalArgumentException("parameter for the json should be implements Sendable");
    }
    return json;
  }

  static <T> T asObject(JsonElement source, JavaType type) throws Exception {
    T obj;
    if (source.isObject()) {
      JsonObject jsonObject = source.asObject();
      jsonObject.removeField(MSG_TYPE);
      obj = mapper.readValue(jsonObject.encode(), type);
    } else if (source.isArray()) {
      obj = mapper.readValue(source.asArray().encode(), type);
    } else {
      throw new IllegalArgumentException("the source should be Json format.");
    }
    return obj;
  }

}
