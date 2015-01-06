package me.streamis.rxbus.test;

import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.VertxRPCInvoker;
import me.streamis.rxbus.rpc.RPCExceptionHandler;
import me.streamis.rxbus.test.service.UserServiceVertx;
import me.streamis.rxbus.test.service.UserServiceVertxServerImpl;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.spi.cluster.impl.hazelcast.HazelcastClusterManagerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class IntegrationServerTest {
  static {
    System.setProperty("vertx.clusterManagerFactory", HazelcastClusterManagerFactory.class.getCanonicalName());
  }

  static String address = "rxBus.address";

  public static void main(String[] args) throws InterruptedException {
    Vertx vertx = VertxFactory.newVertx("10.10.1.33");
    UserServiceVertx userService = new UserServiceVertxServerImpl();
    String serviceName = UserServiceVertx.class.getName();

    RxEventBus rxBus = new RxEventBus(vertx.eventBus(), new RPCExceptionHandler());

    //service mapping, could get this mapping from configuration in json format.
    Map<String, Object> serviceMapping = new HashMap<>();
    serviceMapping.put(serviceName, userService);

    //rpc invoker
    new VertxRPCInvoker(rxBus, serviceMapping, address);
  }

}
