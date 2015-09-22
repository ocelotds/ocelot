# OCELOT
[![Codecov Status](https://codecov.io/github/hhdevelopment/ocelot/coverage.svg?branch=master)](https://codecov.io/github/hhdevelopment)
[![Coverity Status](https://scan.coverity.com/projects/5757/badge.svg)](https://scan.coverity.com/projects/5757)
[![Build Status](https://travis-ci.org/hhdevelopment/ocelot.svg?branch=master)](https://travis-ci.org/hhdevelopment/ocelot)
[![Coverage Status](https://coveralls.io/repos/hhdevelopment/ocelot/badge.svg?branch=master&service=github)](https://coveralls.io/github/hhdevelopment/ocelot?branch=master)
[![Maven](https://img.shields.io/badge/Maven central-2.4.1-blue.svg)](http://search.maven.org/#search|ga|1|ocelot)
[![Maven](https://img.shields.io/badge/OSS Sonatype-2.4.2--SNAPSHOT-lightgrey.svg)](https://oss.sonatype.org/#nexus-search;gav~org.ocelotds~ocelot~~~)

## The best and easiest communication way between java 7 and javascript
#### Forget REST, forget AJAX, forget http, forget protocol, Ocelot uses websocket and do everything for you.

#### Forget limitations about number of connections between browsers and backend. At best 6 simultaneous connections.

[Browsers limitations](http://webdebug.net/2013/12/browser-connection-limit)

[HOW TO](https://github.com/hhdevelopment/ocelot/wiki/howto)

## WAR Package 
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-web</artifactId>
  <version>2.4.1</version>
</dependency>
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-web</artifactId>
  <version>2.4.1</version>
</dependency>
```
## EAR Package 
### Dependencies WAR Modules
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-web</artifactId>
  <version>2.4.1</version>
</dependency>
```
### Dependencies EJB Module
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-core</artifactId>
  <version>2.4.1</version>
</dependency>
```

Ocelot is the new name of zeldads framework, we change the name for refactor and remove right problems.

Ocelot framework allow to call differents services directly from simple classes methods call, like you can do in the backend.   
Don't write WEB Services, focus on business methods, ocelot do communication between business layout and font-end.

Ocelot allow to implement the Message driven bean paragdim but for javascript with topic destination.   
For push message/object to the client.

**Ocelot use one bidirection connection websocket, and is designed for usage in  single page web application.**

The better way, is doing EJB, CDI Beans annotated, but you can call a simple pojo, or soon spring bean.   
If you use mananged classes, you benefits of all features

**Ocelot is develop on reference Java EE server glassfish 4.**
**CDI features, WebSocket features, jsonp features, are provided by glassfish**  

Ocelot can work in servlet container like tomcat without EJB features of course. but requires some extra dependencies and configure them :
 - [cdi](http://docs.jboss.org/weld/reference/1.0.0/en-US/html/environments.html)
 - [jsonp](https://jsonp.java.net/) 

See documentation for [details](http://ocelotds.org).

![codecov.io](http://codecov.io/github/hhdevelopment/ocelot/branch.svg?branch=master)
