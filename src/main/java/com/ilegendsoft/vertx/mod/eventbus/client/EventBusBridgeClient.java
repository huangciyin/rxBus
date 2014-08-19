package com.ilegendsoft.vertx.mod.eventbus.client;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.Shareable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Stream.
 */
public class EventBusBridgeClient implements Shareable, EventBus, Handler<WebSocket> {

  private EventBus eb;
  private final Vertx vertx;
  private WebSocket socket;
  private Map<String, Handler> handlerMap = new ConcurrentHashMap<>();
  private long pingTimerID;
  private Handler<AsyncResult<WebSocket>> completeHandler;
  private static final String BUS_ADDRESS_PREFIX = "eventBusBridgeClient_";
  private final String webSocketURI;
  private static final long PING_INTERVAL = 10000;
  private SocketStatus socketStatus;
  private final AtomicLong replySequence = new AtomicLong(0);
  private HttpClient httpClient;

  private enum MessageCategory {
    SEND, PUBLISH, REGISTER, UNREGISTER, PING
  }

  private enum SocketStatus {
    CONNECTING, OPEN, CLOSING, CLOSED
  }

  public EventBusBridgeClient(Vertx vertx, String prefix) {
    this(vertx, prefix, null, null);
  }

  public EventBusBridgeClient(Vertx vertx, String prefix, HttpClient httpClient, Handler<AsyncResult<WebSocket>> completeHandler) {
    this.vertx = vertx;
    this.eb = vertx.eventBus();
    this.socketStatus = SocketStatus.CONNECTING;
    this.webSocketURI = String.format("/%s/websocket", prefix);
    if (completeHandler != null) this.completeHandler = completeHandler;
    if (httpClient != null) {
      this.httpClient = httpClient;
      this.httpClient.connectWebsocket(webSocketURI, this);
    }
  }

  public void connect(HttpClient httpClient, Handler<AsyncResult<WebSocket>> completeHandler) {
    if (completeHandler != null) this.completeHandler = completeHandler;
    this.httpClient = httpClient;
    this.httpClient.connectWebsocket(webSocketURI, this);
  }

  @Override
  public void handle(WebSocket socket) {
    this.socket = socket;
    this.socketStatus = SocketStatus.OPEN;
    if (completeHandler != null) completeHandler.handle(new DefaultFutureResult<>(socket));
    //send ping
    sendMessage(MessageCategory.PING, null, null, null, null);
    pingTimerID = vertx.setPeriodic(PING_INTERVAL, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        sendMessage(MessageCategory.PING, null, null, null, null);
      }
    });
    //dispose data
    socket.dataHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer event) {
        JsonObject jsonObject = new JsonObject(new String(event.getBytes()));
        final String address = jsonObject.getString("address");
        Object body = jsonObject.getValue("body");

        final Handler handler = handlerMap.remove(address);
        if (handler != null) {
          Integer failureCode = jsonObject.getInteger("failureCode");
          if (failureCode != null) {
            String failureType = jsonObject.getString("failureType");
            String failureMessage = jsonObject.getString("message");
            WrapAsyncResultHandler AsyncResultHandler = (WrapAsyncResultHandler) handler;
            AsyncResultHandler.handleEx(new ReplyException(ReplyFailure.valueOf(failureType), failureCode, failureMessage));
          } else {
            eb.send(address, body);
          }
          //un register handler on the next event loop.
          vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              eb.unregisterHandler(address, handler);
            }
          });
        }
      }
    });
    //socket close handler
    socket.closeHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        socketStatus = SocketStatus.CLOSED;
        vertx.cancelTimer(pingTimerID);
        for (Map.Entry<String, Handler> entry : handlerMap.entrySet()) {
          eb.unregisterHandler(entry.getKey(), entry.getValue());
        }
        handlerMap.clear();
      }
    });
  }

  private <T, R> void sendMessage(MessageCategory category, String address, R body, Handler<Message<T>> replyHandler, WrapAsyncResultHandler<T> asyncResultHandler) {
    final JsonObject envelope = new JsonObject();
    String replyAddress = BUS_ADDRESS_PREFIX + Long.toString(replySequence.incrementAndGet());
    envelope.putString("type", category.toString().toLowerCase());
    if (address != null) envelope.putString("address", address);
    if (body != null) envelope.putValue("body", body);

    Handler<Message<T>> handler = replyHandler != null ? replyHandler : asyncResultHandler != null ? asyncResultHandler : null;
    if (handler != null) {
      envelope.putString("replyAddress", replyAddress);
      eb.registerLocalHandler(replyAddress, handler);
      handlerMap.put(replyAddress, handler);
    }
    checkOpen();
    socket.writeTextFrame(envelope.encode());
  }

  private void checkOpen() {
    if (socketStatus != SocketStatus.OPEN) {
      throw new IllegalStateException("INVALID_STATE_ERR");
    }
  }

  private class WrapAsyncResultHandler<T> implements Handler<Message<T>> {

    private final long timeoutID;
    private final Handler<AsyncResult<Message<T>>> asyncResultHandler;

    private WrapAsyncResultHandler(final Handler<AsyncResult<Message<T>>> asyncResultHandler, long timeout) {
      this.asyncResultHandler = asyncResultHandler;
      this.timeoutID = vertx.setTimer(timeout, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          asyncResultHandler.handle(new DefaultFutureResult<Message<T>>(new ReplyException(ReplyFailure.TIMEOUT)));
        }
      });
    }

    @Override
    public void handle(Message<T> event) {
      vertx.cancelTimer(timeoutID);
      asyncResultHandler.handle(new DefaultFutureResult<>(event));
    }

    public void handleEx(ReplyException ex) {
      vertx.cancelTimer(timeoutID);
      asyncResultHandler.handle(new DefaultFutureResult<Message<T>>(ex));
    }

  }

  @Override
  public void close(final Handler<AsyncResult<Void>> asyncResultHandler) {
    checkOpen();
    socketStatus = SocketStatus.CLOSING;
    socket.close();
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        asyncResultHandler.handle(new DefaultFutureResult<>((Void) null));
      }
    });
  }

  @Override
  public EventBus send(String address, Object object) {
    sendMessage(MessageCategory.SEND, address, object, null, null);
    return this;
  }

  @Override
  public EventBus send(String address, Object object, final Handler<Message> messageHandler) {
    Handler<Message<String>> handler = messageHandler == null ? null : new Handler<Message<String>>() {
      @Override
      public void handle(Message event) {
        messageHandler.handle(event);
      }
    };
    sendMessage(MessageCategory.SEND, address, object, handler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(final String address, final Object object, long timeout, final Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, object, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public <T> EventBus send(String address, JsonObject jsonObject, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, jsonObject, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, JsonObject jsonObject, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, jsonObject, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, JsonObject jsonObject) {
    sendMessage(MessageCategory.SEND, address, jsonObject, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, JsonArray jsonArray, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, jsonArray, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, JsonArray jsonArray, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, jsonArray, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, JsonArray jsonArray) {
    sendMessage(MessageCategory.SEND, address, jsonArray, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Buffer buffer, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, buffer, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Buffer buffer, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, buffer, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Buffer buffer) {
    sendMessage(MessageCategory.SEND, address, buffer, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, byte[] bytes, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, bytes, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, byte[] bytes, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, bytes, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, byte[] bytes) {
    sendMessage(MessageCategory.SEND, address, bytes, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, String string, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, string, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, String string, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, string, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, String string) {
    sendMessage(MessageCategory.SEND, address, string, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Integer integer, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, integer, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Integer integer, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, integer, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Integer integer) {
    sendMessage(MessageCategory.SEND, address, integer, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Long aLong, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aLong, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Long aLong, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aLong, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Long aLong) {
    sendMessage(MessageCategory.SEND, address, aLong, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Float aFloat, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aFloat, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Float aFloat, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aFloat, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Float aFloat) {
    sendMessage(MessageCategory.SEND, address, aFloat, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Double aDouble, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aDouble, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Double aDouble, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aDouble, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Double aDouble) {
    sendMessage(MessageCategory.SEND, address, aDouble, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Boolean aBoolean, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aBoolean, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Boolean aBoolean, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aBoolean, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Boolean aBoolean) {
    sendMessage(MessageCategory.SEND, address, aBoolean, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Short aShort, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aShort, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Short aShort, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aShort, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Short aShort) {
    sendMessage(MessageCategory.SEND, address, aShort, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Character character, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, character, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Character character, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, character, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Character character) {
    sendMessage(MessageCategory.SEND, address, character, null, null);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Byte aByte, Handler<Message<T>> messageHandler) {
    sendMessage(MessageCategory.SEND, address, aByte, messageHandler, null);
    return this;
  }

  @Override
  public <T> EventBus sendWithTimeout(String address, Byte aByte, long timeout, Handler<AsyncResult<Message<T>>> asyncResultHandler) {
    sendMessage(MessageCategory.SEND, address, aByte, null, new WrapAsyncResultHandler<>(asyncResultHandler, timeout));
    return this;
  }

  @Override
  public EventBus send(String address, Byte aByte) {
    sendMessage(MessageCategory.SEND, address, aByte, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Object object) {
    sendMessage(MessageCategory.PUBLISH, address, object, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, JsonObject jsonObject) {
    sendMessage(MessageCategory.PUBLISH, address, jsonObject, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, JsonArray jsonArray) {
    sendMessage(MessageCategory.PUBLISH, address, jsonArray, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Buffer buffer) {
    sendMessage(MessageCategory.PUBLISH, address, buffer, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, byte[] bytes) {
    sendMessage(MessageCategory.PUBLISH, address, bytes, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, String message) {
    sendMessage(MessageCategory.PUBLISH, address, message, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Integer integer) {
    sendMessage(MessageCategory.PUBLISH, address, integer, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Long aLong) {
    sendMessage(MessageCategory.PUBLISH, address, aLong, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Float aFloat) {
    sendMessage(MessageCategory.PUBLISH, address, aFloat, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Double aDouble) {
    sendMessage(MessageCategory.PUBLISH, address, aDouble, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Boolean aBoolean) {
    sendMessage(MessageCategory.PUBLISH, address, aBoolean, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Short aShort) {
    sendMessage(MessageCategory.PUBLISH, address, aShort, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Character character) {
    sendMessage(MessageCategory.PUBLISH, address, character, null, null);
    return this;
  }

  @Override
  public EventBus publish(String address, Byte aByte) {
    sendMessage(MessageCategory.PUBLISH, address, aByte, null, null);
    return this;
  }

  @Override
  public EventBus unregisterHandler(String address, Handler<? extends Message> handler, Handler<AsyncResult<Void>> asyncResultHandler) {
    eb.unregisterHandler(address, handler, asyncResultHandler);
    handlerMap.remove(address);
    sendMessage(MessageCategory.UNREGISTER, address, null, null, null);
    return this;
  }

  @Override
  public EventBus unregisterHandler(String address, Handler<? extends Message> handler) {
    return unregisterHandler(address, handler, null);
  }

  @Override
  public EventBus registerHandler(String address, Handler<? extends Message> handler, final Handler<AsyncResult<Void>> asyncResultHandler) {
    eb.registerHandler(address, handler, asyncResultHandler);
    handlerMap.put(address, handler);
    sendMessage(MessageCategory.REGISTER, address, null, null, null);
    return this;
  }

  @Override
  public EventBus registerHandler(String address, Handler<? extends Message> handler) {
    return registerHandler(address, handler, null);
  }

  @Override
  public EventBus registerLocalHandler(String address, Handler<? extends Message> handler) {
    eb.registerLocalHandler(address, handler);
    handlerMap.put(address, handler);
    return this;
  }

  @Override
  public EventBus setDefaultReplyTimeout(long timeout) {
    return eb.setDefaultReplyTimeout(timeout);
  }

  @Override
  public long getDefaultReplyTimeout() {
    return eb.getDefaultReplyTimeout();
  }

}
