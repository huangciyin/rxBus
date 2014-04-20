package me.streamis.rxbus.test.service.client;

import me.streamis.rxbus.RPCWrapper;
import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.RxMessage;
import me.streamis.rxbus.Sendable;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;
import rx.Observable;
import rx.functions.Func1;

/**
 * 暴露给Vert.x Client端的接口实现
 */
public class UserServiceVertxImpl implements UserServiceVertx {

  private final String serviceImplName = "me.streamis.rxbus.test.service.impl.UserServiceImpl";
  private final RxEventBus rxEventBus;
  private final String serviceAddress;

  public UserServiceVertxImpl(RxEventBus rxEventBus, String address) {
    this.rxEventBus = rxEventBus;
    this.serviceAddress = address;
  }

  private static class ResultWrapper<T> implements Func1<RxMessage, Observable<T>> {
    private Class<T> clazz;

    public ResultWrapper(Class<T> clazz) {
      this.clazz = clazz;
    }

    public ResultWrapper() {
    }

    @Override
    public Observable<T> call(RxMessage rxMessage) {
      if (rxMessage.body() instanceof Sendable && clazz != null) {
        return Observable.just((T) rxMessage.body(clazz));
      } else {
        return Observable.just((T) rxMessage.body());
      }
    }
  }

  @Override
  public Observable<Void> addUser(User user) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "addUser", new Object[]{User.class, user});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Void>());
  }

  @Override
  public Observable<Boolean> updateUserId(int id) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "updateUserId", new Object[]{int.class, id});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Boolean>());
  }

  @Override
  public Observable<Void> addUserToDepartment(User user, Department department) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "addUserToDepartment",
        new Object[]{User.class, user, Department.class, department});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Void>());
  }

  @Override
  public Observable<Department> getDepartmentWithUser(User user) {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "getDepartmentWithUser", new Object[]{User.class, user});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<>(Department.class));
  }

}
