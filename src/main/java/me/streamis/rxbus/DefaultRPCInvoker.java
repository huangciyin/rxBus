package me.streamis.rxbus;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import me.streamis.rxbus.rpc.RPCFailure;
import me.streamis.rxbus.rpc.RPCInvoker;
import me.streamis.rxbus.rpc.RPCWrapper;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DefaultRPCInvoker implements RPCInvoker {

  private final Map<String, Object> serviceMapping;

  public DefaultRPCInvoker(Map<String, Object> serviceMapping) {
    this.serviceMapping = serviceMapping;
  }

  private RPCFailure convert2FailureMessage(Throwable t) {
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


  @Override
  public void call(RPCWrapper rpcWrapper, RxMessage rxMessage) {
    Object service = serviceMapping.get(rpcWrapper.getServiceName());
    Object[] parameters = rpcWrapper.getParameters();

    if (parameters != null && parameters.length > 0) {
      int parameterCount = parameters.length / 2;
      Class[] classes = new Class[parameterCount];
      Object[] values = new Object[parameterCount];

      for (int i = 0; i < parameters.length; i += 2) {
        int index = i / 2;
        try {
          String classStr = (String) parameters[i];
          Object value;
          JavaType javaType = TypeFactory.defaultInstance().constructFromCanonical(classStr);
          Object parameter = parameters[i + 1];
          if (javaType.isCollectionLikeType()) {
            value = JsonParser.asObject(new JsonArray((List) parameter), javaType);
          } else if (javaType.isPrimitive()) {
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
        } catch (Exception e) {
          rxMessage.reply(convert2FailureMessage(e));
        }
      }
      //execute method of service with parameters
      try {
        Method method = service.getClass().getMethod(rpcWrapper.getMethodName(), classes);
        Object result = method.invoke(service, values);
        if (method.getReturnType().equals(Void.TYPE)) {
          rxMessage.reply();
        } else {
          rxMessage.reply(result);
        }
      } catch (Exception e) {
        rxMessage.reply(convert2FailureMessage(e));
      }
    } else {
      //execute method of service without parameters
      try {
        Method method = service.getClass().getMethod(rpcWrapper.getMethodName());
        Object result = method.invoke(service);
        if (method.getReturnType().equals(Void.TYPE)) {
          rxMessage.reply();
        } else {
          rxMessage.reply(result);
        }
      } catch (Exception e) {
        rxMessage.reply(convert2FailureMessage(e));
      }
    }
  }


}
