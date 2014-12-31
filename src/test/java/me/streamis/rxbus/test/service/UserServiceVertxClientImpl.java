package me.streamis.rxbus.test.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.rpc.RPCWrapper;
import me.streamis.rxbus.rpc.ResultWrapper;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.Status;
import me.streamis.rxbus.test.service.domain.User;
import rx.Observable;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class UserServiceVertxClientImpl implements UserServiceVertx {

  private final String serviceName;
  private final RxEventBus rxEventBus;
  private final String serviceAddress;
  private final TypeFactory typeFactory = TypeFactory.defaultInstance();
  private final long timeout = 10000;

  public UserServiceVertxClientImpl(RxEventBus rxEventBus, String address, String serviceName) {
    this.rxEventBus = rxEventBus;
    this.serviceAddress = address;
    this.serviceName = serviceName;
  }

  @Override
  public Observable<Void> hello(Status status) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "hello", new Object[]{Status.class, status});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Void>());
  }

  @Override
  public Observable<Void> addUser(User user) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "addUser", new Object[]{User.class, user});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Void>());
  }

  @Override
  public Observable<String> queryName(String name) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "queryName", new Object[]{String.class, name});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<String>());
  }

  @Override
  public Observable<List<String>> getNames() {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "getNames", null);
    JavaType resultType = typeFactory.constructCollectionType(List.class, String.class);
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<List<String>>(resultType));
  }

  @Override
  public Observable<Boolean> updateUserId(int id) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "updateUserId", new Object[]{int.class, id});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Boolean>());
  }

  @Override
  public Observable<Void> addUserToDepartment(User user, Department department) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "addUserToDepartment",
        new Object[]{User.class, user, Department.class, department});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Void>());
  }

  @Override
  public Observable<Department> getDepartmentWithUser(User user) {
    JavaType resultType = typeFactory.constructFromCanonical(Department.class.getName());
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "getDepartmentWithUser", new Object[]{User.class, user});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Department>(resultType));
  }

  @Override
  public Observable<List<User>> getUsersFromDepartment(Set<Department> departments) {
    JavaType paramType = typeFactory.constructCollectionType(Set.class, Department.class);
    JavaType resultType = typeFactory.constructCollectionType(List.class, User.class);
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "getUsersFromDepartment", new Object[]{paramType, departments});
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<List<User>>(resultType));
  }

  @Override
  public Observable<Void> somethingWrong() {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceName, "somethingWrong", null);
    return rxEventBus.sendWithTimeout(serviceAddress, rpcWrapper, timeout).flatMap(new ResultWrapper<Void>());
  }
}
