package me.streamis.rxbus.test.service.client;


import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;
import rx.Observable;

import java.util.List;
import java.util.Set;

/**
 *async interface of service for vert.x
 */
public interface UserServiceVertx {

  Observable<Void> hello();

  Observable<Void> addUser(User user);

  Observable<Boolean> updateUserId(int id);

  Observable<Void> addUserToDepartment(User user, Department department);

  Observable<Department> getDepartmentWithUser(User user);

  Observable<List<User>> getUsersFromDepartment(Set<Department> departments);

  Observable<Void> somethingWrong();
}
