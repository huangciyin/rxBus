package me.streamis.rxbus;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import rx.Observable;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class RxMessage {

  private Message message;
  private RxExceptionHandler exHandler;
  private SubscriptionHandler observer;
  private String messageType;

  public RxMessage(Message message, RxExceptionHandler exHandler, SubscriptionHandler observer) {
    this.message = message;
    this.exHandler = exHandler;
    this.observer = observer;
  }

  public String getMessageType() {
    if (message.body() instanceof JsonObject) {
      if (messageType == null) {
        messageType = ((JsonObject) message.body()).getString(JsonParser.MSG_TYPE);
      }
      return messageType;
    } else
      throw new IllegalArgumentException("message should be json format.");
  }

  private boolean isFail(JsonObject json) {
    return json.getString(JsonParser.MSG_TYPE) != null && json.getString(JsonParser.MSG_TYPE).equals(JsonParser.FAILED);
  }

  public <T> T body(JavaType type) {
    Object body = message.body();
    if (body instanceof JsonObject && type != null) {
      JsonObject result = (JsonObject) body;
      if (this.messageType == null) this.messageType = result.getString(JsonParser.MSG_TYPE);
      if (isFail(result)) {
        result.removeField(JsonParser.FAILED);
        observer.fireError(exHandler.handle(result));
      } else try {
        return JsonParser.asObject(result, type);
      } catch (Exception e) {
        observer.fireError(e);
      }
    } else if (body instanceof JsonArray && type != null) {
      JsonArray resultArray = (JsonArray) body;
      try {
        return JsonParser.asObject(resultArray, type);
      } catch (Exception e) {
        observer.fireError(e);
      }
    } else {
      observer.fireError(new IllegalArgumentException("unKnow format of message.."));
    }
    return null;
  }

  public <T> T body(Class<T> clazz) {
    return body(TypeFactory.defaultInstance().constructType(clazz));
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
    if (object instanceof Sendable || object instanceof Collection || object instanceof Map) {
      try {
        message.reply(JsonParser.asJson(object), handler);
      } catch (Exception e) {
        handler.fireError(e);
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
        handler.fireError(e);
      }
    } else {
      message.replyWithTimeout(object, timeout, handler);
    }
    return Observable.create(handler);
  }
}
