package me.streamis.rxbus.test.service;

import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;

/**
 *
 */
public interface UserService {

  void addUser(User user);

  boolean updateUserId(int id);

  void addUserToDepartment(User user, Department department);

  Department getDepartmentWithUser(User user);

  //TODO: parameter with list

  //TODO: Exception
}
