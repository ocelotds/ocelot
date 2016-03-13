# OCELOT
## More  details on [ocelotds.org](http://ocelotds.org)
[![Codecov Status](https://codecov.io/github/ocelotds/ocelot/coverage.svg?branch=master)](https://codecov.io/github/ocelotds)
[![Coverity Status](https://scan.coverity.com/projects/7127/badge.svg)](https://scan.coverity.com/projects/7127)
[![Build Status](https://travis-ci.org/ocelotds/ocelot.svg?branch=master)](https://travis-ci.org/ocelotds/ocelot)
[![Maven](https://img.shields.io/badge/Maven central-2.9.0-blue.svg)](http://search.maven.org/#search|ga|1|ocelot)
[![Maven](https://img.shields.io/badge/OSS Sonatype-2.9.1--SNAPSHOT-lightgrey.svg)](https://oss.sonatype.org/#nexus-search;gav~org.ocelotds~ocelot~~~)

## The best and easiest communication way between java 7 and javascript

## WAR Package 
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-core</artifactId>
  <version>2.9.0</version>
</dependency>
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-web</artifactId>
  <version>2.9.0</version>
</dependency>
```
## EAR Package 
### Dependencies WAR Modules
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-web</artifactId>
  <version>2.9.0</version>
</dependency>
```
### Dependencies EJB Module
```xml
<dependency>
  <groupId>org.ocelotds</groupId>
  <artifactId>ocelot-core</artifactId>
  <version>2.9.0</version>
</dependency>
```

Ocelotds is the new name of zeldads framework, we change the name for refactor and remove right problems.

Ocelot framework allow to call differents services directly from simple classes methods call, like you can do in the backend.   
Don't write WEB Services, focus on business methods, ocelot do communication between business layout and font-end.

Ocelot allow to implement the Message driven bean paragdim but for javascript with topic destination.   
For push message/object to the client.

See documentation for [details](http://ocelotds.org).

![codecov.io](http://codecov.io/github/ocelotds/ocelot/branch.svg?branch=master)
