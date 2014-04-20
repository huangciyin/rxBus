package me.streamis.rxbus.test.service.domain;

import me.streamis.rxbus.Sendable;

/**
 *
 */
public class User implements Sendable {
  private int id;
  private String Name;
  private Department department;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }
}
