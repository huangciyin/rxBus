package me.streamis.rxbus.test.service;

import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;

import java.util.List;
import java.util.Set;

/**
 *
 */
public interface UserService {

  void hello();

  void addUser(User user);

  boolean updateUserId(int id);

  void addUserToDepartment(User user, Department department);

  Department getDepartmentWithUser(User user);

  List<User> getUsersFromDepartment(Set<Department> departments);

  void somethingWrong() throws IllegalArgumentException;
}
