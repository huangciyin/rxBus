package me.streamis.rxbus;

import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public interface RxExceptionHandler <T extends RuntimeException> {

  T handle(JsonObject json);

  T handle(Exception ex);
}
