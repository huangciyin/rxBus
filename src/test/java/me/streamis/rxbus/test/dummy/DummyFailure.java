package me.streamis.rxbus.test.dummy;

import me.streamis.rxbus.MessageType;
import me.streamis.rxbus.Sendable;

@MessageType("__FAILED__")
public class DummyFailure implements Sendable {
  private String errorMsg;
  private String errorCode;

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public String toString() {
    return "DummyFailure{" +
        "errorMsg='" + errorMsg + '\'' +
        ", errorCode='" + errorCode + '\'' +
        '}';
  }
}
