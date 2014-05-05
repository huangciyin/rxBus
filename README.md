rxBus
=====

Enhance EventBus of vert.x with rx pattern and Java Bean, which refer to the project [mod-rxJava](https://github.com/vert-x/mod-rxvertx)
You can send POJO over EventBus by this module, and other week language also can decode POJO in json format.

All the object that send by EventBus hava to implement interface of `Sendable` which mean this Object should be encode as json format.
there is example [DummySender](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/dummy/DummySender.java)

    @MessageType("WithDummySender")
    public class DummySender implements Sendable {
      private int id;
      private String name;
      private List<String> codes;
    }

Ok, maybe you have notice an annotation of `@MessageType`, this annotation indicate a message type. We could get type of object with method `instanceOf` in java, it doesn't work in week language. so we wrap message with adding a field of string which indicate message type. You could get this type by `rxMessage.getMessageType();` in java.

In javascipt, you could get message type explicitly by `message.__MSG_TYPE__`. Yes, field of `__MSG_TYPE__` be added by mod for distinguish type from message.

Ok, next, you could invoke rxBus send Object as you wish.
there is a example which include all cases.
[RxEventBusExample](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/RxEventBusTest.java)


RPC invoke 
====

The RPC invoking is common in distribution service, and we could provide service with vert.x in old project which all the request is synchronism.

It's very easy to using rpc in rxBus.

* mapping service's class.
* make a wrapper for vert.x's client to invoke.

First, we mock a service.

    public interface UserService {
      void addUser(User user);
      
      Department getDepartmentWithUser(User user);
      
      List<User> getUsersFromDepartment(Set<Department> departments);
    }

as you will see, all the method is synchronism, we should make warpper for client of vert.x to invoke.

    public interface UserServiceVertx {
      Observable<Void> addUser(User user);

      Observable<Department> getDepartmentWithUser(User user);

      Observable<List<User>> getUsersFromDepartment(Set<Department> departments);
    }

The difference of above interface is type of return. The detail of implement could be find in [UserServiceVertxImpl](https://github.com/stream1984/rxBus/blob/master/src/test/java/me/streamis/rxbus/test/service/client/UserServiceVertxImpl.java)

Then listen request from client, we make register for rpc invoking.

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
here is simple example about how to make rpc in client.

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

    
We have to be care of type of return which is not be wrapped by `RxMessage`, since rpc don't have to reply message to original sender.

Compile & install
====

`git clone https://github.com/stream1984/rxBus.git`

and

`mvn install`

then deploy `me.streamis~rxBus~0.1.0` to your project, with mod.json `include : "me.streamis~rxBus~0.1.0"`

Or

depend on this lib directly with maven:

    <dependency>
      <groupId>me.streamis</groupId>
      <artifactId>rxBus</artifactId>
      <version>0.1.0</version>
    </dependency>
    

any feedbacks is welcome.






