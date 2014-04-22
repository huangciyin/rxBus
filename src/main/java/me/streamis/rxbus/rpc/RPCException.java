package me.streamis.rxbus.rpc;

/**
 *
 */
public class RPCException extends Exception {

  public RPCException(String message) {
    super(message);
  }

  public RPCException(String message, String detailMessage) {
    super(message, new Throwable(detailMessage));
  }

}
