package me.streamis.rxbus.test;

import me.streamis.rxbus.RxEventBus;
import me.streamis.rxbus.RxMessage;
import me.streamis.rxbus.test.dummy.*;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class RxEventBusTest extends TestVerticle {

  private RxEventBus rxBus;
  private String address = "rxBus.address";


  public void start() {
    initialize();
    rxBus = new RxEventBus(vertx.eventBus(), new DummyExceptionHandler());
    startTests();
  }

  @Test
  public void echo() {
    //register
    rxBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage rxMessage) {
        String body = rxMessage.body();
        assertEquals("ping", body);
        rxMessage.reply("pong");
      }
    });

    //sender
    Observable<RxMessage> obs = rxBus.send(address, "ping");
    obs.subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage stringRxMessage) {
        assertEquals("pong", stringRxMessage.body());
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void echoSendable() {
    //register
    Observable<RxMessage> obsRegister = rxBus.registerHandler(address);
    obsRegister.subscribe(
        new Action1<RxMessage>() {
          @Override
          public void call(RxMessage rxMessage) {
            //we should know message type, since we dependency it to convert message to Java Object.
            assertEquals("WithDummySender", rxMessage.getMessageType());
            DummySender sender = rxMessage.body(DummySender.class);
            assertEquals(sender.getName(), "dummy-name");
            assertEquals(sender.getId(), 100);
            assertEquals(2, sender.getCodes().size());
            DummyReceive receive = new DummyReceive();
            receive.setName("receive");
            receive.setResult(true);
            receive.setCodes(sender.getCodes());
            rxMessage.reply(receive);
          }
        }
    );

    //send
    Observable<RxMessage> obs = rxBus.send(address, getDummySender());
    obs.subscribe(
        new Action1<RxMessage>() {
          @Override
          public void call(RxMessage message) {
            DummyReceive receive = message.body(DummyReceive.class);
            assertEquals("DummyReceive", message.getMessageType());
            assertTrue(receive.isResult());
            assertEquals(receive.getName(), "receive");
            assertEquals(2, receive.getCodes().size());
            VertxAssert.testComplete();
          }
        },
        new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            throwable.printStackTrace();
            VertxAssert.testComplete();
          }
        }
    );
  }


  @Test
  public void serial() {
    //register
    Observable<RxMessage> obsRegister = rxBus.registerHandler(address);
    obsRegister.subscribe(
        new Action1<RxMessage>() {
          @Override
          public void call(RxMessage rxMessage) {
            DummyReceive receive = new DummyReceive();
            DummySender sender = rxMessage.body(DummySender.class);
            receive.setName(sender.getName());
            rxMessage.reply(receive);
          }
        }
    );

    //sender
    rxBus.send(address, getDummySender()).flatMap(new Func1<RxMessage, Observable<RxMessage>>() {
      @Override
      public Observable<RxMessage> call(RxMessage dummyReceiveRxMessage) {
        DummyReceive receive = dummyReceiveRxMessage.body(DummyReceive.class);
        DummySender sender = new DummySender();
        sender.setName(receive.getName() + "1");
        return rxBus.send(address, sender);
      }
    }).flatMap(new Func1<RxMessage, Observable<RxMessage>>() {
      @Override
      public Observable<RxMessage> call(RxMessage dummyReceiveRxMessage) {
        DummyReceive receive = dummyReceiveRxMessage.body(DummyReceive.class);
        DummySender sender = new DummySender();
        sender.setName(receive.getName() + "2");
        return rxBus.send(address, sender);
      }
    }).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage dummyReceiveRxMessage) {
        DummyReceive receive = dummyReceiveRxMessage.body(DummyReceive.class);
        assertEquals("dummy-name12", receive.getName());
        VertxAssert.testComplete();
      }
    });
  }

  @Test
  public void fail() {
    rxBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage message) {
        DummyFailure failureObject = new DummyFailure();
        failureObject.setErrorCode("errorCode");
        failureObject.setErrorMsg("errorMessage");
        message.reply(failureObject);
      }
    });

    Observable<RxMessage> obs = rxBus.send(address, getDummySender());
    obs.subscribe(
        new Action1<RxMessage>() {
          public void call(RxMessage dummyReceive) {
            VertxAssert.fail("should be json error");
          }
        },
        new Action1<Throwable>() {
          public void call(Throwable e) {
            assertNotNull(e);
            assertTrue(e instanceof DummyException);
            VertxAssert.testComplete();
          }
        }
    );
  }


  private DummySender getDummySender() {
    DummySender dummy = new DummySender();
    dummy.setId(100);
    dummy.setName("dummy-name");
    List<String> codes = new ArrayList<>();
    codes.add("one");
    codes.add("two");
    dummy.setCodes(codes);
    return dummy;
  }
}
