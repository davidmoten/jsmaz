# jsmaz
<a href="https://travis-ci.org/davidmoten/jsmaz"><img src="https://travis-ci.org/davidmoten/jsmaz.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/jsmaz/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/jsmaz)<br/>
[![codecov](https://codecov.io/gh/davidmoten/jsmaz/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/jsmaz)<br/>

com.github.davidmoten.jsmaz is a Java port of the Smaz short string compression algorithm by Salvatore Sanfilippo and released as a C library at: https://github.com/antirez/smaz. This Java port was built using https://github.com/RyanAD/jsmaz and https://github.com/tmbo/scala-smaz/. 

**Status:** in development

Features
* supports UTF-8 character set
* good test coverage

## Getting started
Add this to your pom.xml:

```xml
<dependency>
  <groupId>com.github.davidmoten</groupId>
  <artifactId>jsmaz</artifactId>
  <version>VERSION_HERE</version>
</dependency>
``` 

# Usage
```java
String a = "this is a simple test";

// compress to 10 bytes
byte[] compressed = Smaz.compress(a);

// decompress
String b = Smaz.decompress(compressed);
assertEquals(a, b);
```


