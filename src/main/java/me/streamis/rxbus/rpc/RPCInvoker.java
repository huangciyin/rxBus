package me.streamis.rxbus.rpc;

import me.streamis.rxbus.RxMessage;

/**
 * RPC call parser
 */
public interface RPCInvoker {

  public static final String RPCMessage = "__RxRpcRequest__";

  void call(RPCWrapper rpcWrapper, RxMessage rxMessage);

  void shutdown();
}
