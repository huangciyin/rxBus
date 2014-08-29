package me.streamis.rxbus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public final class RxJsonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

  static {
    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
  }


  public static <T> JsonObject toJsonObject(T obj) {
    JsonObject json;
    if (obj instanceof Map) {
      json = new JsonObject((Map) obj);
    } else {
      try {
        json = new JsonObject(mapper.writeValueAsString(obj));
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("convert to json format exception.", e);
      }
    }
    return json;
  }

  public static <T> String toJsonString(T obj) {
    String jsonStr;
    if (obj instanceof Map) {
      jsonStr = new JsonObject((Map) obj).encode();
    } else {
      try {
        jsonStr = mapper.writeValueAsString(obj);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("convert to json format exception.", e);
      }
    }
    return jsonStr;
  }

  public static <T> JsonArray toJsonArray(T obj) {
    JsonArray json;
    if (obj instanceof Collection) {
      json = new JsonArray();
      JsonArray jsonArray = json.asArray();
      for (Object element : (Collection) obj) jsonArray.add(toJsonObject(element));
    } else {
      throw new IllegalArgumentException("the type of object should be collection.");
    }
    return json;
  }


  public static <T> T toPOJO(JsonElement source, Class clazz) {
    T obj;
    if (source.isObject()) {
      obj = toPOJO(source, typeFactory.constructType(clazz));
    } else if (source.isArray()) {
      throw new IllegalArgumentException("the type of source is collection, please make class to JavaType");
    } else {
      throw new IllegalArgumentException("the source should be Json format.");
    }
    return obj;
  }

  public static <T> T toPOJO(JsonElement source, JavaType type) {
    T obj;
    try {
      if (source.isObject()) {
        JsonObject jsonObject = source.asObject();
        obj = mapper.readValue(jsonObject.encode(), type);
      } else if (source.isArray()) {
        obj = mapper.readValue(source.asArray().encode(), type);
      } else {
        throw new IllegalArgumentException("the source should be Json format.");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("the source can not be convert to " + type.toString(), e);
    }
    return obj;
  }

  public static <T> T toPOJO(String str, Class clazz) {
    T obj;
    try {
      obj = (T) mapper.readValue(str, clazz);
    } catch (IOException e) {
      throw new IllegalArgumentException("the source can not be convert to " + clazz.getName(), e);
    }
    return obj;
  }

}
