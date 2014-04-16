package me.streamis.rxbus.test;

/**
 *
 */
public class DummyException extends RuntimeException {
  public DummyException() {
  }

  public DummyException(String message) {
    super(message);
  }

  public DummyException(String message, Throwable cause) {
    super(message, cause);
  }

  public DummyException(Throwable cause) {
    super(cause);
  }
}
