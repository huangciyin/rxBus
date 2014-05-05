package me.streamis.rxbus.rpc;

import me.streamis.rxbus.MessageType;
import me.streamis.rxbus.Sendable;

import java.util.Arrays;

/**
 * RPC bean
 */
@MessageType("__RxRpcRequest__")
public class RPCWrapper implements Sendable {
  private String serviceName;
  private String methodName;
  private Object[] parameters;

  public RPCWrapper() {

  }

  public RPCWrapper(String serviceName, String methodName, Object[] parameters) {
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String interfaceName) {
    this.serviceName = interfaceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Object[] getParameters() {
    return parameters;
  }

  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "RPCWrapper{" +
        "serviceName='" + serviceName + '\'' +
        ", methodName='" + methodName + '\'' +
        ", parameters=" + Arrays.toString(parameters) +
        '}';
  }
}
