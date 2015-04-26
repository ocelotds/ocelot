# OCELOT
## The best way to communicate java and javascript 

Ocelot framework allow to call differents services directly from simple classes methods call, like you can do in the backend.

Ocelot allow to implement the Message driven bean paragdim but for javascript with topic destination.
For push message/object to the client.

Ocelot use one bidirection connection websocket.

The better way, is doing EJB annotated, but you can call a simple pojo, or soon spring bean.

### How to use : 
Add dependency in your maven web project

```xml
  <dependency>
      <groupId>fr.hhdev</groupId>
      <artifactId>ocelot-core</artifactId>
      <version>1.0.0</version>
  </dependency>
```
Annotate services
```java
@Stateless
@DataService(resolverid = Constants.Resolver.EJB)
public class TestEJBService {
```
Compilation generate javascript stub by introspection of annotated classes

Add core and generated services in html page (these scripts are gived by servlets)

```html
	<body>
		<script src="ocelot-services.js" type="text/javascript"></script>
		<script src="ocelot-core.js" type="text/javascript"></script>
```

Use services directement in your code

```javascript
var srv = new TestEJBService();
var token = srv.getMessage(Math.floor(Math.random()*10));
token.onResult = function (msg) {
  doSomethingWithMsg(msg); // string message
};
token.onFault = function (fault) {
	alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
};
```
Message Driven bean features, can be use for implement a chat for example or notify something to the client.
```javascript
var mdb = new Mdb("mytopic");
mdb.onMessage = function (msg) {
	addMessage(msg);
};
mdb.subscribe();
```
```java
	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	public void publish() {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId("mytopic");
		messageToClient.setResult("Message From server");
		wsEvent.fire(messageToClient);
	}
```





