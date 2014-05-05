rxBus
=====

Enhance EventBus of vert.x with rx pattern and Java Bean, we make this have refrence the project [mod-rxJava](https://github.com/vert-x/mod-rxvertx)
This moulde let you send Java Object over EventBus, and other week language also could decode Java Object in json format.

All the object that send with EventBus hava to implement interface of `Sendable` which mean this Object should be encode as json format.
there is example [DummySender](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/dummy/DummySender.java)

    ```java
    @MessageType("WithDummySender")
    public class DummySender implements Sendable {
      private int id;
      private String name;
      private List<String> codes;
    }

Ok, maybe you have notice an annotation of `@MessageType`, this annotation indicate a message type. We could get type of object with method `instanceOf` in java, this is not work in  week language. so we wrap message add a field of string which indicate message type. You could get this type by `rxMessage.getMessageType();` in java.

In js, you should get message type explicitly by `message.__MSG_TYPE__`. Yes, field of `__MSG_TYPE__` be added by mod for tell from type of message.

Ok, next, you could invoke rxBus send Object as you wish.
there is a example which include all the situation.
[RxEventBusExample](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/RxEventBusTest.java)


RPC invoke 
====

The RPC invoking is common in distribution service, and we could provide service with vert.x in old project which all the request is synchronism.

It's very easy to useing rpc in rxBus.

* mapping service's class.
* make a wrapper for vert.x's client to invoke.

First, we mock a service.

    ```java
    public interface UserService {
      void addUser(User user);
      
      Department getDepartmentWithUser(User user);
      
      List<User> getUsersFromDepartment(Set<Department> departments);
    }

as you can see, all the method is synchronism, we should make warpper for client of vert.x to invoke.

    ```java
    public interface UserServiceVertx {
      Observable<Void> addUser(User user);

      Observable<Department> getDepartmentWithUser(User user);

      Observable<List<User>> getUsersFromDepartment(Set<Department> departments);
    }

The difference of above interface is type of return. The detail of implement could be find in [UserServiceVertxImpl](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/service/client/UserServiceVertxImpl.java)

The next thing is listen request from client, we make register for rpc invoking.

    ```java
    rpcInvoker = new DefaultRPCInvoker(serviceMapping);
    
    rxBus.registerHandler(address).subscribe(new Action1<RxMessage>() {
      @Override
      public void call(RxMessage rxMessage) {
        //get the type of message
        switch (rxMessage.getMessageType()) {
          case RPCInvoker.RPCMessage:
            RPCWrapper rpcWrapper = rxMessage.body(RPCWrapper.class);
            rpcInvoker.call(rpcWrapper, rxMessage);
            break;
          default:
            break;
        }
      }
    });

`RPCWrapper` include all the rpc metadata, we invoking target service with this parameter.
there is simple example about how to make rpc in client.

    ```java
    Department department = new Department();
    department.setId(1);
    department.setName("IT");

    User user = new User();
    user.setId(1);
    user.setName("stream");
    user.setDepartment(department);

    Observable<Void> result = userServiceVertx.addUser(user);

    result.subscribe(new Action1<Void>() {
      @Override
      public void call(Void aVoid) {
        VertxAssert.testComplete();
      }
    });

We have to be care that type of return is not be wrapped by `RxMessage`, since rpc don't have to reply message to original sender.

Compile & install
====

`git clone https://github.com/stream1984/rxBus.git`

and

`mvn install`

then deploy `me.streamis~rxBus~0.1.0` to your project, with mod.json `include : "me.streamis~rxBus~0.1.0"`

Or

dependency this lib directly with maven:

    <dependency>
      <groupId>me.streamis</groupId>
      <artifactId>rxBus</artifactId>
      <version>0.1.0</version>
    </dependency>
    

any feedback is welcome.






