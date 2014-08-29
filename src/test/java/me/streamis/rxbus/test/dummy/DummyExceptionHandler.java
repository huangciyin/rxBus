package me.streamis.rxbus.test.dummy;

import me.streamis.rxbus.RxExceptionHandler;
import me.streamis.rxbus.RxJsonUtils;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public class DummyExceptionHandler implements RxExceptionHandler<DummyException> {

  @Override
  public DummyException handle(JsonObject json) {
    DummyFailure failure = RxJsonUtils.toPOJO(json, DummyFailure.class);
    return new DummyException(failure.getErrorCode(), failure.getErrorMsg());
  }


}
