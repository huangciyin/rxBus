package me.streamis.rxbus.test;

import me.streamis.rxbus.*;
import me.streamis.rxbus.rpc.RPCException;
import me.streamis.rxbus.rpc.RPCExceptionHandler;
import me.streamis.rxbus.rpc.RPCInvoker;
import me.streamis.rxbus.rpc.RPCWrapper;
import me.streamis.rxbus.test.service.UserService;
import me.streamis.rxbus.test.service.client.UserServiceVertx;
import me.streamis.rxbus.test.service.client.UserServiceVertxImpl;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.Status;
import me.streamis.rxbus.test.service.domain.User;
import me.streamis.rxbus.test.service.UserServiceImpl;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import rx.Observable;
import rx.functions.Action1;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 */
public class RxRpcTest extends TestVerticle {

  private RxEventBus rxBus;
  private String address = "rxBus.address";
  private RPCInvoker rpcInvoker;
  private UserServiceVertx userServiceVertx;

  public void start() {
    initialize();
    //service
    UserService userService = new UserServiceImpl();
    String serviceName = UserServiceImpl.class.getName();

    rxBus = new RxEventBus(vertx.eventBus(), new RPCExceptionHandler());

    //service mapping, could get this mapping from configuration in json format.
    Map<String, Object> serviceMapping = new HashMap<>();
    serviceMapping.put(serviceName, userService);

    //rpc invoker
    rpcInvoker = new DefaultRPCInvoker(serviceMapping);
    registerService();

    //service for client
    userServiceVertx = new UserServiceVertxImpl(rxBus, address, serviceName);
    startTests();
  }

  /**
   * register bus handler with address
   */
  private void registerService() {
    rxBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage rxMessage) {
        switch (rxMessage.getMessageType()) {
          case RPCInvoker.RPCMessage:
            RPCWrapper rpcWrapper = rxMessage.body(RPCWrapper.class);
            rpcInvoker.call(rpcWrapper, rxMessage);
            break;
          default:
            break;
        }
      }
    });
  }

  @Test
  public void hello() {
    Observable<Void> result = userServiceVertx.hello(Status.SUCCESS);
    result.subscribe(new Action1<Void>() {
      @Override
      public void call(Void aVoid) {
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcCallAddUser() {
    Department department = new Department();
    department.setId(1);
    department.setName("IT");

    User user = new User();
    user.setId(1);
    user.setName("stream");
    user.setDepartment(department);

    Observable<Void> result = userServiceVertx.addUser(user);

    result.subscribe(new Action1<Void>() {
      @Override
      public void call(Void aVoid) {
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcUpdateUserId() {
    Observable<Boolean> result = userServiceVertx.updateUserId(2);
    result.subscribe(new Action1<Boolean>() {
      @Override
      public void call(Boolean isSuccess) {
        assertTrue(isSuccess);
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcAddUserToDepartment() {
    Department department = new Department();
    department.setId(2);
    department.setName("RESEARCH");

    User user = new User();
    user.setId(1);
    user.setName("stream");
    user.setDepartment(department);

    userServiceVertx.addUserToDepartment(user, department).subscribe(new Action1<Void>() {
      @Override
      public void call(Void aVoid) {
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcGetDepartmentWithUser() {
    User user = new User();
    user.setId(1);
    userServiceVertx.getDepartmentWithUser(user).subscribe(new Action1<Department>() {
      @Override
      public void call(Department department) {
        assertEquals(1, department.getId());
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcGetUsersFromDepartment() {
    Set<Department> departments = new HashSet<>();
    Department department = new Department();
    departments.add(department);

    userServiceVertx.getUsersFromDepartment(departments).subscribe(new Action1<List<User>>() {
      @Override
      public void call(List<User> users) {
        assertEquals(1, users.size());
        assertEquals(1, users.get(0).getId());
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void rpcSomethingWrong() {
    userServiceVertx.somethingWrong().subscribe(new Action1<Void>() {
      @Override
      public void call(Void aVoid) {
        VertxAssert.fail("should be failed");
      }
    }, new Action1<Throwable>() {
      @Override
      public void call(Throwable throwable) {
        assertTrue(throwable instanceof RPCException);
        VertxAssert.testComplete();
      }
    });
  }


}
