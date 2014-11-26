package me.streamis.rxbus;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import me.streamis.rxbus.rpc.RPCFailure;
import me.streamis.rxbus.rpc.RPCInvoker;
import me.streamis.rxbus.rpc.RPCWrapper;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import rx.functions.Action1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by stream.
 */
abstract class AbsRPCInvoker implements RPCInvoker {

  protected final Map<String, Object> serviceMapping;
  protected final String address;
  protected final RxEventBus rxEventBus;

  public AbsRPCInvoker(RxEventBus rxEventBus, Map<String, Object> serviceMapping, String address) {
    this.rxEventBus = rxEventBus;
    this.serviceMapping = serviceMapping;
    this.address = address;
    registryServices();
  }

  private void registryServices() {
    rxEventBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage rxMessage) {
        if (rxMessage.getMessageType().equals(RPCInvoker.RPCMessage)) {
          RPCWrapper rpcWrapper = rxMessage.body(RPCWrapper.class);
          AbsRPCInvoker.this.call(rpcWrapper, rxMessage);
        }
      }
    });
  }

  @Override
  public void shutdown() {
    rxEventBus.unRegisterHandler(address);
  }

  protected RPCFailure convert2FailureMessage(Throwable t) {
    RPCFailure rpcFailure = new RPCFailure();
    if (t instanceof InvocationTargetException) {
      t = ((InvocationTargetException) t).getTargetException();
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    rpcFailure.setMessage(t.getMessage());
    rpcFailure.setStackMessage(sw.toString());
    return rpcFailure;
  }

  protected Object callResult(RPCWrapper rpcWrapper) throws Exception {
    Object service = serviceMapping.get(rpcWrapper.getServiceName());
    Object[] parameters = rpcWrapper.getParameters();
    Method method;
    if (parameters != null && parameters.length > 0) {
      int parameterCount = parameters.length / 2;
      Class[] classes = new Class[parameterCount];
      Object[] values = new Object[parameterCount];

      for (int i = 0; i < parameters.length; i += 2) {
        int index = i / 2;

        String classStr = (String) parameters[i];
        Object value;
        JavaType javaType = TypeFactory.defaultInstance().constructFromCanonical(classStr);
        Object parameter = parameters[i + 1];
        if (javaType.isCollectionLikeType()) {
          value = JsonParser.asObject(new JsonArray((List) parameter), javaType);
        } else if (javaType.isPrimitive()) {
          value = parameter;
        } else if (javaType.getRawClass().equals(String.class)) {
          value = parameter;
        } else if (javaType.isEnumType()) {
          Enum[] enums = (Enum[]) javaType.getRawClass().getEnumConstants();
          value = parameter;
          for (Enum e : enums) {
            if (e.name().equals(value)) {
              value = e;
              break;
            }
          }
        } else {
          value = JsonParser.asObject(new JsonObject((Map) parameter), javaType);
        }
        classes[index] = javaType.getRawClass();
        values[index] = value;
      }
      //execute method of service with parameters
      method = service.getClass().getMethod(rpcWrapper.getMethodName(), classes);
      return method.invoke(service, values);
    } else {
      //execute method of service without parameters
      method = service.getClass().getMethod(rpcWrapper.getMethodName());
      return method.invoke(service);
    }

  }

}
