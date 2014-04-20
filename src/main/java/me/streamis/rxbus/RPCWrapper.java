package me.streamis.rxbus;

import java.util.Arrays;

/**
 * 包装请求成为RPC bean
 */
@MessageType("__RxRpcRequest__")
public class RPCWrapper implements Sendable {
  private String interfaceName;
  private String methodName;
  private Object[] parameters;

  public RPCWrapper() {

  }

  public RPCWrapper(String interfaceName, String methodName, Object[] parameters) {
    this.interfaceName = interfaceName;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
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
        "interfaceName='" + interfaceName + '\'' +
        ", methodName='" + methodName + '\'' +
        ", parameters=" + Arrays.toString(parameters) +
        '}';
  }
}
