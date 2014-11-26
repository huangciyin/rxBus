package me.streamis.rxbus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import rx.Observable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class RxEventBus {

  private EventBus eventBus;
  private final RxExceptionHandler exHandler;
  private Map<String, ReceiveHandler> receiveHandlers = new ConcurrentHashMap<>();

  public RxEventBus(EventBus eventBus, RxExceptionHandler exHandler) {
    this.eventBus = eventBus;
    this.exHandler = exHandler;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  private boolean isFail(JsonObject json) {
    return json.getString(JsonParser.MSG_TYPE) != null && json.getString(JsonParser.MSG_TYPE).equals(JsonParser.FAILED);
  }

  protected class ReceiveHandler<R> extends SubscriptionHandler<RxMessage, Message<R>> {
    @Override
    public void handle(Message<R> message) {
      R body = message.body();
      if (body instanceof JsonObject) {
        JsonObject result = (JsonObject) body;
        if (isFail(result)) {
          result.removeField(JsonParser.MSG_TYPE);
          fireError(exHandler.handle(result));
          return;
        }
      }
      fireNext(new RxMessage(message, exHandler, this));
    }
  }

  protected class AsyncReceiveHandler<R> extends SubscriptionHandler<RxMessage, AsyncResult<Message<R>>> {
    @Override
    public void handle(AsyncResult<Message<R>> r) {
      if (r.succeeded()) {
        R body = r.result().body();
        if (body instanceof JsonObject) {
          JsonObject result = (JsonObject) body;
          if (isFail(result)) {
            result.removeField(JsonParser.MSG_TYPE);
            fireError(exHandler.handle(result));
            return;
          }
        }
        fireNext(new RxMessage(r.result(), exHandler, this));
      } else {
        fireError(r.cause());
      }
    }
  }

  public <R> Observable<RxMessage> send(final String address, final Object object) {
    return Observable.create(new ReceiveHandler<R>() {
      @Override
      public void execute() {
        if (object instanceof Sendable || object instanceof Map) {
          try {
            eventBus.send(address, JsonParser.asJson(object).asObject(), this);
          } catch (Exception e) {
            fireError(e);
          }
        } else if (object instanceof Collection) {
          try {
            eventBus.send(address, JsonParser.asJson(object).asArray(), this);
          } catch (Exception e) {
            fireError(e);
          }
        } else {
          eventBus.send(address, object, (Handler) this);
        }
      }
    });
  }

  public <R> Observable<RxMessage> sendWithTimeout(final String address, final Object object, final long timeout) {
    return Observable.create(new AsyncReceiveHandler<R>() {
      @Override
      public void execute() {
        if (object instanceof Sendable || object instanceof Map) {
          try {
            eventBus.sendWithTimeout(address, JsonParser.asJson(object).asObject(), timeout, this);
          } catch (Exception e) {
            fireError(e);
          }
        } else if (object instanceof Collection) {
          try {
            eventBus.sendWithTimeout(address, JsonParser.asJson(object).asArray(), timeout, this);
          } catch (Exception e) {
            fireError(e);
          }
        } else {
          eventBus.sendWithTimeout(address, object, timeout, this);
        }
      }
    });
  }

  public <T> Observable<RxMessage> registerLocalHandler(final String address) {
    ReceiveHandler<T> handler = new ReceiveHandler<T>() {
      @Override
      public void execute() {
        eventBus.registerLocalHandler(address, this);
      }
    };
    receiveHandlers.put(address, handler);
    return Observable.create(handler);
  }

  public <T> Observable<RxMessage> registerHandler(final String address) {
    ReceiveHandler<T> handler = new ReceiveHandler<T>() {
      @Override
      public void execute() {
        eventBus.registerHandler(address, this);
      }
    };
    receiveHandlers.put(address, handler);
    return Observable.create(handler);
  }

  public void unRegisterHandler(String address) {
    ReceiveHandler handler = receiveHandlers.remove(address);
    if (handler != null)
      eventBus.unregisterHandler(address, handler);
  }

  public void unRegisterAllHandlers() {
    for (String address : receiveHandlers.keySet()) {
      unRegisterHandler(address);
    }
    receiveHandlers.clear();
  }

}
