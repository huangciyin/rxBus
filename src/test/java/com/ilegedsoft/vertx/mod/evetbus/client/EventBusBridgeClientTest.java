package com.ilegedsoft.vertx.mod.evetbus.client;

import com.ilegendsoft.vertx.mod.eventbus.client.EventBusBridgeClient;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

/**
 * Created by stream.
 */
public class EventBusBridgeClientTest extends TestVerticle {

  //This eventbus is based on sockJS client.
  private EventBus eb;

  @Override
  public void start() {
    initialize();
    //make a server
    HttpServer server = vertx.createHttpServer();
    JsonArray permitted = new JsonArray();
    permitted.add(new JsonObject());

    SockJSServer sockJSServer = vertx.createSockJSServer(server);
    sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
    server.listen(8080, new Handler<AsyncResult<HttpServer>>() {
      @Override
      public void handle(AsyncResult<HttpServer> event) {
        registerEventBus(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> event) {
            HttpClient httpClient = vertx.createHttpClient();
            httpClient.setHost("localhost").setPort(8080);
            //
            eb = new EventBusBridgeClient(vertx, httpClient, "eventbus", new Handler<AsyncResult<WebSocket>>() {
              @Override
              public void handle(AsyncResult<WebSocket> event) {
                //we have to make sure connection have been established.
                startTests();
              }
            });
          }
        });
      }
    });
  }

  private void registerEventBus(final Handler<AsyncResult<Void>> completeHandler) {
    vertx.eventBus().registerHandler("eventbus", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> event) {
        VertxAssert.assertNotNull(event.body());
        VertxAssert.assertEquals("test", event.body().getString("test"));
        event.reply(new JsonObject().putString("test", "reply-test"));
      }
    }, completeHandler);
  }

  private void assertSend(Message<JsonObject> event) {
    VertxAssert.assertNotNull(event.body());
    VertxAssert.assertEquals("reply-test", event.body().getString("test"));
  }

  @Test
  public void send() {
    final JsonObject msg = new JsonObject().putString("test", "test");
    eb.send("eventbus", msg, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> event) {
        assertSend(event);
        VertxAssert.testComplete();
      }
    });
  }


  @Test
  public void sendWithTimeout() {
    final JsonObject msg = new JsonObject().putString("test", "test");
    eb.sendWithTimeout("eventbus", msg, 3000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> event) {
        assertSend(event.result());
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void noHandlerException() {
    eb.sendWithTimeout("errorAddress", new JsonObject(), 3000, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> event) {
        VertxAssert.assertTrue("should be failed.", event.failed());
        VertxAssert.assertTrue(event.cause() instanceof ReplyException);
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void registerPublish() {
    final JsonObject msg = new JsonObject().putString("test", "test");
    eb.registerHandler("someAddress", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> event) {
        VertxAssert.assertNotNull(event.body());
        VertxAssert.assertEquals("test", event.body().getString("test"));
        VertxAssert.testComplete();
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        eb.publish("someAddress", msg);
      }
    });
  }


}
