package me.streamis.rxbus;

import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public interface RxExceptionHandler <T extends Exception> {

  T handle(JsonObject json);


}
