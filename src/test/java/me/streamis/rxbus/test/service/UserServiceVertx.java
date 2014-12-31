package me.streamis.rxbus.test.service;


import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.Status;
import me.streamis.rxbus.test.service.domain.User;
import rx.Observable;

import java.util.List;
import java.util.Set;

/**
 *async interface of service for vert.x
 */
public interface UserServiceVertx {

  Observable<Void> hello(Status status);

  Observable<Void> addUser(User user);

  Observable<Boolean> updateUserId(int id);

  Observable<String> queryName(String name);

  Observable<List<String>> getNames();

  Observable<Void> addUserToDepartment(User user, Department department);

  Observable<Department> getDepartmentWithUser(User user);

  Observable<List<User>> getUsersFromDepartment(Set<Department> departments);

  Observable<Void> somethingWrong();
}
