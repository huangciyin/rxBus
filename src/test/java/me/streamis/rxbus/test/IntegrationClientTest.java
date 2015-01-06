package me.streamis.rxbus.test;

import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.rpc.RPCExceptionHandler;
import me.streamis.rxbus.test.service.UserServiceVertx;
import me.streamis.rxbus.test.service.UserServiceVertxClientImpl;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.spi.cluster.impl.hazelcast.HazelcastClusterManagerFactory;
import rx.functions.Action1;

import java.util.List;

/**
 * Created by stream on 1/6/15.
 */
public class IntegrationClientTest {
  static {
    System.setProperty("vertx.clusterManagerFactory", HazelcastClusterManagerFactory.class.getCanonicalName());
  }

  static String address = "rxBus.address";
  public static void main(String[] args) {
    Vertx vertx = VertxFactory.newVertx("10.10.1.33");
    String serviceName = UserServiceVertx.class.getName();
    RxEventBus rxBus = new RxEventBus(vertx.eventBus(), new RPCExceptionHandler());

    UserServiceVertx userServiceVertx = new UserServiceVertxClientImpl(rxBus, address, serviceName);

    userServiceVertx.getNames().subscribe(
        new Action1<List<String>>() {
          @Override
          public void call(List<String> strings) {
            System.out.println("OK");
          }
        },
        new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            if (throwable instanceof ReplyException) {
              System.out.println(((ReplyException) throwable).failureType());
              System.out.println(((ReplyException) throwable).failureCode());
            }
            throwable.printStackTrace();
          }
        });
  }
}
