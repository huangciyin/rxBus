package me.streamis.rxbus.test;

import me.streamis.rxbus.*;
import me.streamis.rxbus.test.dummy.DummyExceptionHandler;
import me.streamis.rxbus.test.service.UserService;
import me.streamis.rxbus.test.service.client.UserServiceVertx;
import me.streamis.rxbus.test.service.client.UserServiceVertxImpl;
import me.streamis.rxbus.test.service.domain.Department;
import me.streamis.rxbus.test.service.domain.User;
import me.streamis.rxbus.test.service.impl.UserServiceImpl;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import rx.Observable;
import rx.functions.Action1;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class RxRpcTest extends TestVerticle {

  private RxEventBus rxBus;
  private String address = "rxBus.address";
  private RPCInvoker rpcInvoker;
  private Map<String, Object> serviceMapping;
  private UserServiceVertx userServiceVertx;
  private UserService userService;

  private static class RPCExceptionHandler implements RxExceptionHandler {

    @Override
    public RuntimeException handle(JsonObject json) {
      return new RuntimeException(json.getString("error"));
    }

    @Override
    public RuntimeException handle(Exception ex) {
      return new RuntimeException(ex);
    }
  }

  public void start() {
    initialize();
    //service
    userService = new UserServiceImpl();

    rxBus = new RxEventBus(vertx.eventBus(), new DummyExceptionHandler());

    //service mapping
    serviceMapping = new HashMap<>();
    serviceMapping.put("me.streamis.rxbus.test.service.impl.UserServiceImpl", userService);

    //rpc invoker
    rpcInvoker = new DefaultRPCInvoker(serviceMapping, new RPCExceptionHandler());
    registerService();

    //service for client
    userServiceVertx = new UserServiceVertxImpl(rxBus, address);
    startTests();
  }

  private void registerService() {
    rxBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage rxMessage) {
        switch (rxMessage.getType()) {
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


}
