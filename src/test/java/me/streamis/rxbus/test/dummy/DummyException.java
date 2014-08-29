package me.streamis.rxbus.test.dummy;

/**
 *
 */
public class DummyException extends RuntimeException {

  private String code;

  public DummyException(String code, String message) {
    super(message);
    this.code = code;
  }


}
