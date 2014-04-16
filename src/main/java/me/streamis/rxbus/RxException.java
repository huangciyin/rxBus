package me.streamis.rxbus;

import java.util.Map;

/**
 *
 */
public class RxException extends RuntimeException {
  //错误码
  private String code;

  //错误原因
  private String reason;

  //可能携带的一些数据
  private Map<String, Object> data;

  //异常
  private Throwable ex;

  public RxException(String code, String reason, Map<String, Object> data) {
    this.code = code;
    this.reason = reason;
    this.data = data;
  }

  public RxException(String code, String reason, Throwable ex) {
    this.code = code;
    this.reason = reason;
    this.ex = ex;
  }

  public RxException(String code, String reason) {
    this.code = code;
    this.reason = reason;
  }

  public RxException(Throwable ex) {
    this.ex = ex;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public Throwable getEx() {
    return ex;
  }

  public void setEx(Throwable ex) {
    this.ex = ex;
  }

  @Override
  public String toString() {
    return "RxException{" +
        "code='" + code + '\'' +
        ", reason='" + reason + '\'' +
        ", data=" + data +
        ", ex=" + ex +
        '}';
  }
}
