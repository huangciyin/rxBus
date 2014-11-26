package me.streamis.rxbus.test.service;

import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.Status;
import me.streamis.rxbus.test.service.domain.User;
import org.vertx.testtools.VertxAssert;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by stream.
 */
public class UserServiceVertxServerImpl implements UserServiceVertx {

  @Override
  public Observable<Void> hello(Status status) {
    VertxAssert.assertEquals(status, Status.SUCCESS);
    System.out.println("test null parameter");
    return Observable.just(null);
  }

  @Override
  public Observable<Void> addUser(User user) {
    assertEquals("stream", user.getName());
    assertEquals(1, user.getId());
    assertEquals(1, user.getDepartment().getId());
    assertEquals("IT", user.getDepartment().getName());
    return Observable.just(null);
  }

  @Override
  public Observable<Boolean> updateUserId(int id) {
    assertEquals(2, id);
    return Observable.just(true);
  }

  @Override
  public Observable<String> queryName(String name) {
    assertEquals("name", name);
    return Observable.just("stream");
  }

  @Override
  public Observable<Void> addUserToDepartment(User user, Department department) {
    assertEquals("stream", user.getName());
    assertEquals(1, user.getId());
    assertEquals(2, user.getDepartment().getId());
    assertEquals("RESEARCH", user.getDepartment().getName());
    return Observable.just(null);
  }

  @Override
  public Observable<Department> getDepartmentWithUser(User user) {
    assertEquals(1, user.getId());
    Department department = new Department();
    department.setId(1);
    return Observable.just(department);
  }

  @Override
  public Observable<List<User>> getUsersFromDepartment(Set<Department> departments) {
    assertEquals(1, departments.size());
    List<User> users = new ArrayList<>();
    User user = new User();
    user.setId(1);
    users.add(user);
    return Observable.just(users);
  }

  @Override
  public Observable<Void> somethingWrong() {
    return Observable.error(new IllegalArgumentException("wrong"));
  }
}
