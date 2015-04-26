# OCELOT
## The best way to communicate java and javascript 
Ocelot is the new name of zeldads framework, we change the name for refactor and remove right problems.

Ocelot framework allow to call differents services directly from simple classes methods call, like you can do in the backend.

Ocelot allow to implement the Message driven bean paragdim but for javascript with topic destination.
For push message/object to the client.

Ocelot use one bidirection connection websocket.

The better way, is doing EJB annotated, but you can call a simple pojo, or soon spring bean.

# How to use : 
## Dependencies
Add dependency in your maven web project

```xml
  <dependency>
      <groupId>fr.hhdev</groupId>
      <artifactId>ocelot-core</artifactId>
      <version>1.0.0</version>
  </dependency>
```
## Annotate services
Set service is accesible from javasccript front end
```java
@Stateless
@DataService(resolverid = Constants.Resolver.EJB)
public class TestEJBService {
```
Compilation generate javascript stub by introspection of annotated classes

## Add Framework to html
Add core and generated services in html page (these scripts are gived by servlets)

```html
	<body>
		<script src="ocelot-services.js" type="text/javascript"></script>
		<script src="ocelot-core.js" type="text/javascript"></script>
```

## Use it
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

## Notifications 
Message Driven bean features, can be use for implement a chat for example or notify something to the client.

### create MDB
In javascript, do an instance of Mdb

```javascript
var mdb = new Mdb("mytopic"); // This Mdb listen topic "mytopic"
mdb.onMessage = function (msg) {
   doSomethingWithMsg(msg); // string message
};
mdb.subscribe();
```

### publish message
In java, publish message to all subcriber clients
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

## SPI, extedns Ocelot
How to extends Ocelot.

In case or defaults resolver doen't access to your service.

Create librairie include implementations of DataServiceResolver

 - Dependency : 
```xml
  <dependency>
      <groupId>fr.hhdev</groupId>
      <artifactId>ocelot-core</artifactId>
      <version>1.0.0</version>
  </dependency>
```
 - Class
```java
@DataServiceResolverId("MyRID")
public class MyResolver implements DataServiceResolver {

	@Override
	public Object resolveDataService(String dataService) throws DataServiceException {
	// how to get dataService implementation
	}
```
And on the services 
```java
@DataService(resolverid = "MyRID")
public class MyService {
```







