package me.streamis.rxbus;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 */
public class DefaultRPCInvoker implements RPCInvoker {

  private final Map<String, Object> serviceMapping;
  private final RxExceptionHandler exHandler;

  public DefaultRPCInvoker(Map<String, Object> serviceMapping, RxExceptionHandler exHandler) {
    this.serviceMapping = serviceMapping;
    this.exHandler = exHandler;
  }

  @Override
  public void call(RPCWrapper rpcWrapper, RxMessage rxMessage) {
    Object service = serviceMapping.get(rpcWrapper.getInterfaceName());
    Object[] parameters = rpcWrapper.getParameters();

    if (parameters.length > 0) {
      int parameterCount = parameters.length / 2;
      Class[] classes = new Class[parameterCount];
      Object[] values = new Object[parameterCount];

      for (int i = 0; i < parameterCount; i++) {
        if (i % 2 == 0) {
          try {
            classes[i] = Class.forName((String) parameters[i]);
          } catch (ClassNotFoundException e) {
            //TODO:exception should be fireError
            rxMessage.reply(exHandler.handle(e));
          }
        }
        else
          values[i] = parameters[i];
      }
      //execute method of service with parameters
      try {
        Method method = service.getClass().getMethod(rpcWrapper.getMethodName(), classes);
        if (method.getReturnType().equals(Void.TYPE)) {
          rxMessage.reply();
        } else {
          rxMessage.reply(method.invoke(service, values));
        }
      } catch (Exception e) {
        //TODO:exception should be fireError
        rxMessage.reply(exHandler.handle(e));
      }
    } else {
      //execute method of service without parameters
      try {
        Method method = service.getClass().getMethod(rpcWrapper.getMethodName());
        if (method.getReturnType().equals(Void.TYPE)) {
          rxMessage.reply();
        } else {
          rxMessage.reply(method.invoke(service));
        }
      } catch (Exception e) {
        //TODO:exception should be fireError
        rxMessage.reply(exHandler.handle(e));
      }
    }


  }



}
