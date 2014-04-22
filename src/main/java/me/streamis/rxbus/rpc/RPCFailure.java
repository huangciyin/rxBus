package me.streamis.rxbus.rpc;

import me.streamis.rxbus.MessageType;
import me.streamis.rxbus.Sendable;

/**
 *
 */
@MessageType("__FAILED__")
public class RPCFailure implements Sendable {

  private String message;
  private String stackMessage;

  public RPCFailure() {
  }

  public RPCFailure(String message) {
    this.message = message;
  }

  public RPCFailure(String message, String stackMessage) {
    this.message = message;
    this.stackMessage = stackMessage;
  }

  public String getStackMessage() {
    return stackMessage;
  }

  public void setStackMessage(String stackMessage) {
    this.stackMessage = stackMessage;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
