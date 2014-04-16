package me.streamis.rxbus;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;

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

  static <T> JsonObject asJson(T obj) throws Exception {
    JsonObject json;
    if (obj instanceof Sendable) {
      json = new JsonObject(mapper.writeValueAsString(obj));
      MessageType messageType = obj.getClass().getAnnotation(MessageType.class);
      if (messageType != null && messageType.value() != null) {
        json.putString(MSG_TYPE, messageType.value());
      }
    } else {
      throw new IllegalArgumentException("parameter for the json should be implements Sendable");
    }
    return json;
  }

  static <T> T asObject(JsonObject source, Class<T> clazz) throws IOException {
    source.removeField(MSG_TYPE);
    return mapper.readValue(source.encode(), clazz);

  }


}
