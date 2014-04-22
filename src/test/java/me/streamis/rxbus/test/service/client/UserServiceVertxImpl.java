package me.streamis.rxbus.test.service.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import me.streamis.rxbus.rpc.RPCWrapper;
import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.RxMessage;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;
import org.vertx.java.core.json.JsonElement;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class UserServiceVertxImpl implements UserServiceVertx {

  private final String serviceImplName = "me.streamis.rxbus.test.service.UserServiceImpl";
  private final RxEventBus rxEventBus;
  private final String serviceAddress;
  private final TypeFactory typeFactory;

  public UserServiceVertxImpl(RxEventBus rxEventBus, String address) {
    this.rxEventBus = rxEventBus;
    this.serviceAddress = address;
    this.typeFactory = TypeFactory.defaultInstance();
  }

  private static class ResultWrapper<T> implements Func1<RxMessage, Observable<T>> {
    private JavaType type;

    public ResultWrapper(JavaType type) {
      this.type = type;
    }

    public ResultWrapper() {
    }

    @Override
    public Observable<T> call(RxMessage rxMessage) {
      if (rxMessage.body() instanceof JsonElement && type != null) {
        return Observable.just((T) rxMessage.body(type));
      } else {
        return Observable.just((T) rxMessage.body());
      }
    }
  }


  @Override
  public Observable<Void> hello() {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "hello", null);
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Void>());
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
    JavaType resultType = typeFactory.constructFromCanonical(Department.class.getName());
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "getDepartmentWithUser", new Object[]{User.class, user});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Department>(resultType));
  }

  @Override
  public Observable<List<User>> getUsersFromDepartment(Set<Department> departments) {
    JavaType paramType = typeFactory.constructCollectionType(Set.class, Department.class);
    JavaType resultType = typeFactory.constructCollectionType(List.class, User.class);
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "getUsersFromDepartment", new Object[]{paramType, departments});
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<List<User>>(resultType));
  }

  @Override
  public Observable<Void> somethingWrong() {
    RPCWrapper rpcWrapper = new RPCWrapper(serviceImplName, "somethingWrong", null);
    return rxEventBus.send(serviceAddress, rpcWrapper).flatMap(new ResultWrapper<Void>());
  }
}
