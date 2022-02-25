<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![Maven Build](https://github.com/artipie/files-adapter/actions/workflows/maven.yml/badge.svg)](https://github.com/artipie/files-adapter/actions/workflows/maven.yml)
[![Javadoc](http://www.javadoc.io/badge/com.artipie/files-adapter.svg)](http://www.javadoc.io/doc/com.artipie/files-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/files-adapter.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/files-adapter)

This is a simple storage, used in a few other projects.

This is the dependency you need:

```xml
<dependency>
  <groupId>com.artipie</groupId>
  <artifactId>files-adapter</artifactId>
  <version>[...]</version>
</dependency>
```

Read the [Javadoc](http://www.javadoc.io/doc/com.artipie/files-adapter)
for more technical details.

## Running

For simple cases or for debugging you may want to start files-adapter as HTTP server.
Just build it and start with:
```bash
mvn package dependency:copy-dependencies
java -cp "target/files-adapter-1.0-SNAPSHOT.jar:target/dependency/*" com.artipie.files.FilesSlice
```
This command builds service and start it with in-memory storage on localhost on `8080` port.

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+.

