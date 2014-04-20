package me.streamis.rxbus;

/**
 * RPC call parser
 */
public interface RPCInvoker {

  public static final String RPCMessage = "__RxRpcRequest__";

  void call(RPCWrapper rpcWrapper, RxMessage rxMessage);
}
