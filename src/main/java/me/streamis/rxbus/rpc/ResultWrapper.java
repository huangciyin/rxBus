package me.streamis.rxbus.rpc;

import com.fasterxml.jackson.databind.JavaType;
import me.streamis.rxbus.RxMessage;
import org.vertx.java.core.json.JsonElement;
import rx.Observable;
import rx.functions.Func1;

/**
 *
 */
public class ResultWrapper<T> implements Func1<RxMessage, Observable<T>> {

  private JavaType type;

  public ResultWrapper(JavaType type) {
    this.type = type;
  }

  public ResultWrapper() {
  }

  @Override
  public Observable<T> call(RxMessage rxMessage) {
    if (rxMessage.body() instanceof JsonElement && type != null) {
      return Observable.just((T) rxMessage.body(type));
    } else {
      return Observable.just((T) rxMessage.body());
    }
  }
}
