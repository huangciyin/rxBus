package me.streamis.rxbus.test.service.domain;

import me.streamis.rxbus.Sendable;

/**
 *
 */
public class Department implements Sendable{

  private int id;
  private String name;

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
}
