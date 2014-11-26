package me.streamis.rxbus;

import me.streamis.rxbus.rpc.RPCInvoker;
import me.streamis.rxbus.rpc.RPCWrapper;
import rx.Observable;
import rx.functions.Action1;

import java.util.Map;

/**
 * Created by stream.
 */
public class VertxRPCInvoker extends AbsRPCInvoker implements RPCInvoker {

  public VertxRPCInvoker(RxEventBus rxEventBus, Map<String, Object> serviceMapping, String address) {
    super(rxEventBus, serviceMapping, address);
  }

  @Override
  public void call(RPCWrapper rpcWrapper, final RxMessage rxMessage) {
    try {
      Observable<Object> result = (Observable) callResult(rpcWrapper);
      assert result != null;
      result.subscribe(
          new Action1<Object>() {
            @Override
            public void call(Object o) {
              rxMessage.reply(o);
            }
          },
          new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
              rxMessage.reply(convert2FailureMessage(throwable));
            }
          });
    } catch (Exception e) {
      rxMessage.reply(convert2FailureMessage(e));
    }
  }
}
