eventbus-bridge-client
======================

Wrap up eventbus client with websocket httpclient in Vert.x

## Usage

Since this class implements EventBus, so you just ues it as EventBus.
Make sure threre is httpclient to the server which have been bridge with sockjs.

    HttpClient httpClient = vertx.createHttpClient();
	httpClient.setHost("localhost").setPort(8080);
	
    EventBus eb = new EventBusBridgeClient(vertx, "eventbus", httpClient, new Handler<AsyncResult<WebSocket>>() {
              @Override
              public void handle(AsyncResult<WebSocket> event) {
                //we have to make sure connection have been established.
                start();
              }
            });

You could get completed example in the [Test](https://github.com/ilegendsoft/eventbus-bridge-client/blob/master/src/test/java/com/ilegedsoft/vertx/mod/evetbus/client/EventBusBridgeClientTest.java)

## Install

You have to clone this project and then maven install it to the local repos.
Then include it as mod in your mod.json `com.ilegendsoft~eventbus-brdige-client~1.0.0`.
This project it is not runnable mod, so just instance it directly.

You could also copy only file to your project :)




