package me.streamis.rxbus.test;

import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.VertxRPCInvoker;
import me.streamis.rxbus.rpc.RPCException;
import me.streamis.rxbus.rpc.RPCExceptionHandler;
import me.streamis.rxbus.test.service.UserServiceVertx;
import me.streamis.rxbus.test.service.UserServiceVertxClientImpl;
import me.streamis.rxbus.test.service.UserServiceVertxServerImpl;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.Status;
import me.streamis.rxbus.test.service.domain.User;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import rx.Observable;
import rx.functions.Action1;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by stream.
 */
public class VertxRxRpcTest extends TestVerticle {

  private UserServiceVertx userServiceVertx;

  public void start() {
    initialize();
    //service
    UserServiceVertx userService = new UserServiceVertxServerImpl();
    String serviceName = UserServiceVertx.class.getName();

    RxEventBus rxBus = new RxEventBus(vertx.eventBus(), new RPCExceptionHandler());

    //service mapping, could get this mapping from configuration in json format.
    Map<String, Object> serviceMapping = new HashMap<>();
    serviceMapping.put(serviceName, userService);

    String address = "rxBus.address";

    //rpc invoker
    new VertxRPCInvoker(rxBus, serviceMapping, address);
    userServiceVertx = new UserServiceVertxClientImpl(rxBus, address, serviceName);
    startTests();
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
  public void rpcQueryName() {
    userServiceVertx.queryName("name").subscribe(
        new Action1<String>() {
          @Override
          public void call(String s) {
            VertxAssert.assertEquals("stream", s);
            VertxAssert.testComplete();
          }
        },
        new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            VertxAssert.fail(throwable.getMessage());
          }
        }
    );
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

  @Test
  public void getNames() {
    userServiceVertx.getNames().subscribe(
        new Action1<List<String>>() {
          @Override
          public void call(List<String> strings) {
            assertTrue(strings.size() == 2);
            VertxAssert.testComplete();
          }
        },
        new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            throwable.printStackTrace();
          }
        });
  }

}
