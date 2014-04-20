package me.streamis.rxbus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import rx.Observable;

import java.io.IOException;

/**
 *
 */
public class RxMessage {

  private Message message;
  private RxExceptionHandler exHandler;
  private SubscriptionHandler observer;
  private String type;

  public RxMessage(Message message, RxExceptionHandler exHandler, SubscriptionHandler observer) {
    this.message = message;
    this.exHandler = exHandler;
    this.observer = observer;
  }

  public String getType() {
    if (message.body() instanceof JsonObject) {
      if (type == null) {
        type = ((JsonObject) message.body()).getString(JsonParser.MSG_TYPE);
      }
      return type;
    } else
      throw new IllegalArgumentException("message should be json format.");
  }

  private boolean isFail(JsonObject json) {
    return json.getString(JsonParser.MSG_TYPE) != null && json.getString(JsonParser.MSG_TYPE).equals(JsonParser.FAILED);
  }

  public <T> T body(Class clazz) {
    Object body = message.body();
    if (body instanceof JsonObject && clazz != null) {
      JsonObject result = (JsonObject) body;
      if (type == null) this.type = result.getString(JsonParser.MSG_TYPE);
      if (isFail(result)) {
        //过滤逻辑错误
        result.removeField(JsonParser.FAILED);
        observer.fireError(exHandler.handle(result));
      } else try {
        return (T) JsonParser.asObject(result, clazz);
      } catch (IOException e) {
        observer.fireError(exHandler.handle(e));
      }
    }
    return null;
  }

  public <T> T body() {
    return (T) message.body();
  }


  public String replyAddress() {
    return message.replyAddress();
  }

  public <T> Message<T> coreMessage() {
    return message;
  }

  public void reply() {
    message.reply();
  }


  protected class ReplyHandler<R> extends SubscriptionHandler<RxMessage, Message<R>> {
    @Override
    public void handle(Message message) {
      fireResult(new RxMessage(message, exHandler, this));
    }
  }

  protected class AsyncReplyHandler<R> extends SubscriptionHandler<RxMessage, AsyncResult<Message<R>>> {
    @Override
    public void handle(AsyncResult<Message<R>> r) {
      if (r.succeeded()) {
        fireResult(new RxMessage(r.result(), exHandler, this));
      } else {
        fireError(r.cause());
      }
    }
  }

  public Observable<RxMessage> reply(final Object object) {
    ReplyHandler handler = new ReplyHandler<>();
    if (object instanceof Sendable) {
      try {
        message.reply(JsonParser.asJson(object), handler);
      } catch (Exception e) {
        observer.fireError(e);
      }
    } else {
      message.reply(object, handler);
    }
    return Observable.create(handler);
  }

  public <R> Observable<RxMessage> replyWithTimeout(final Object object, final long timeout) {
    AsyncReplyHandler<R> handler = new AsyncReplyHandler<>();
    if (object instanceof Sendable) {
      try {
        message.replyWithTimeout(JsonParser.asJson(object), timeout, handler);
      } catch (Exception e) {
        observer.fireError(e);
      }
    } else {
      message.replyWithTimeout(object, timeout, handler);
    }
    return Observable.create(handler);
  }
}
