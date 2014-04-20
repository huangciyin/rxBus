package me.streamis.rxbus.test.dummy;

import me.streamis.rxbus.MessageType;
import me.streamis.rxbus.Sendable;

import java.util.List;

/**
 *
 */
@MessageType("DummyReceive")
public class DummyReceive implements Sendable {
  private String name;
  private List<String> codes;
  private boolean result;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(List<String> codes) {
    this.codes = codes;
  }

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }
}
