package me.streamis.rxbus.test.dummy;

import me.streamis.rxbus.RxExceptionHandler;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public class DummyExceptionHandler implements RxExceptionHandler<DummyException> {
  @Override
  public DummyException handle(JsonObject json) {
    return new DummyException("json error");
  }


}
