[TICKET JIRA](https://issues.sonatype.org/browse/OSSRH-15324)
# OCELOT
## The best and easiest communication way between java and javascript
### Dependencies

```xml
<dependency>
  <groupId>fr.hhdev</groupId>
  <artifactId>ocelot-core</artifactId>
  <version>1.0.1</version>
</dependency>
```

Ocelot is the new name of zeldads framework, we change the name for refactor and remove right problems.

Ocelot framework allow to call differents services directly from simple classes methods call, like you can do in the backend.

Ocelot allow to implement the Message driven bean paragdim but for javascript with topic destination.
For push message/object to the client.

**Ocelot use one bidirection connection websocket, and is designed for usage in  single page web application.**

The better way, is doing EJB annotated, but you can call a simple pojo, or soon spring bean.

**Ocelot is develop on reference JEE server glassfish 4.**
**CDI features, WebSocket features, jsonp features, are provided by glassfish**  
**Ocelot can be work in servlet container like tomcat without EJB features of course. but need add some dependencies and configure them :**
 - cdi [weld](http://docs.jboss.org/weld/reference/1.0.0/en-US/html/environments.html)
 - webSocket : [tomcat](http://tomcat.apache.org/tomcat-7.0-doc/web-socket-howto.html)
 - add jsonp is probably necessary too


