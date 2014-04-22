package me.streamis.rxbus.rpc;

import me.streamis.rxbus.RxExceptionHandler;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public class RPCExceptionHandler implements RxExceptionHandler<RPCException> {
  @Override
  public RPCException handle(JsonObject json) {
    return new RPCException(json.getString("message"), json.getString("stackMessage"));
  }
}
