package me.streamis.rxbus;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
    JsonElement jsonElement;
    if (obj instanceof Sendable) {
      jsonElement = new JsonObject(mapper.writeValueAsString(obj));
      MessageType messageType = obj.getClass().getAnnotation(MessageType.class);
      if (messageType != null) {
        JsonObject json = jsonElement.asObject();
        if (!messageType.value().equals("")) {
          json.putString(MSG_TYPE, messageType.value());
        } else if (messageType.fail()) {
          json.putString(MSG_TYPE, FAILED);
        } else {
          json.putString(MSG_TYPE, obj.getClass().getSimpleName());
        }
      }
    } else if (obj instanceof Collection) {
      jsonElement = new JsonArray();
      JsonArray jsonArray = jsonElement.asArray();
      for (Object element : (Collection) obj) {
        JavaType javaType = TypeFactory.defaultInstance().uncheckedSimpleType(element.getClass());
        if (javaType.isPrimitive() || javaType.getRawClass() == String.class) {
          jsonArray.add(element);
        } else {
          jsonArray.add(asJson(element));
        }
      }
    } else if (obj instanceof Map) {
      jsonElement = new JsonObject((Map) obj);
    } else {
      throw new IllegalArgumentException("parameter for the json should be implements Sendable");
    }
    return jsonElement;
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
