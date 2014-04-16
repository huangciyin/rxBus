package me.streamis.rxbus.test;

import me.streamis.rxbus.MessageType;
import me.streamis.rxbus.Sendable;

import java.util.List;

/**
 *
 */
@MessageType("WithDummySender")
public class DummySender implements Sendable {
  private int id;
  private String name;
  private List<String> codes;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

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

  @Override
  public String toString() {
    return "DummySender{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", codes=" + codes +
        '}';
  }
}
