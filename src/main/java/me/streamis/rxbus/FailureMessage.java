package me.streamis.rxbus;

/**
 *
 */
@MessageType(fail = true)
public class FailureMessage implements Sendable {
  private int code;
  private String message;

  public FailureMessage(int code) {
    this.code = code;
  }

  public FailureMessage(String message) {
    this.message = message;
  }

  public FailureMessage(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
