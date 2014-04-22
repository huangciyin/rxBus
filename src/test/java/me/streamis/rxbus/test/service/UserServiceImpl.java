package me.streamis.rxbus.test.service;

import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class UserServiceImpl implements UserService {

  @Override
  public void hello() {
    System.out.println("test null parameter");
  }

  @Override
  public void addUser(User user) {
    assertEquals("stream", user.getName());
    assertEquals(1, user.getId());
    assertEquals(1, user.getDepartment().getId());
    assertEquals("IT", user.getDepartment().getName());
  }

  @Override
  public boolean updateUserId(int id) {
    assertEquals(2, id);
    return true;
  }

  @Override
  public void addUserToDepartment(User user, Department department) {
    assertEquals("stream", user.getName());
    assertEquals(1, user.getId());
    assertEquals(2, user.getDepartment().getId());
    assertEquals("RESEARCH", user.getDepartment().getName());
  }

  @Override
  public Department getDepartmentWithUser(User user) {
    assertEquals(1, user.getId());
    Department department = new Department();
    department.setId(1);
    return department;
  }

  @Override
  public List<User> getUsersFromDepartment(Set<Department> departments) {
    assertEquals(1, departments.size());
    List<User> users = new ArrayList<>();
    User user = new User();
    user.setId(1);
    users.add(user);

    return users;
  }

  @Override
  public void somethingWrong() throws IllegalArgumentException {
    throw new IllegalArgumentException("wrong");
  }
}
