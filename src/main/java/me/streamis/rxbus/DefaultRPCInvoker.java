package me.streamis.rxbus;

import me.streamis.rxbus.rpc.RPCInvoker;
import me.streamis.rxbus.rpc.RPCWrapper;

import java.util.Map;

/**
 *
 */
public class DefaultRPCInvoker extends AbsRPCInvoker implements RPCInvoker {

  public DefaultRPCInvoker(RxEventBus rxEventBus, Map<String, Object> serviceMapping, String address) {
    super(rxEventBus, serviceMapping, address);
  }

  @Override
  public void call(RPCWrapper rpcWrapper, RxMessage rxMessage) {
    try {
      Object result = callResult(rpcWrapper);
      rxMessage.reply(result);
    } catch (Exception e) {
      rxMessage.reply(convert2FailureMessage(e));
    }
  }


}
