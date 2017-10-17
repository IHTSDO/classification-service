Snomed Classification Service
====================================

## Overview
A standalone service for the classification of SNOMED CT ontologies. 

The service takes an RF2 Snapshot archive and RF2 Delta archive and produces RF2-like results to be further processed by a terminology server. 

## Quick Start
Use Maven to build the executable jar and run:
```bash
mvn clean package
java -Xmx3g -jar target/classification-service*.jar
```
Access the service API documentation at [http://localhost:8089/classification-service](http://localhost:8089/classification-service)

## Setup
For each SNOMED CT Edition you would like to classify against put an RF2 archive containing the Snapshot into the directory **/store/releases**

When creating a classification using **POST /classifications** the _previousRelease_ parameter should be the filename of one of these archives.
See API documentation for more details. The documentation is hosted by the service at [http://localhost:8080/](http://localhost:8080/).

### Changing the Default Configuration
This is a Spring Boot application. Configuration options can be placed after the jar parameter on the command line or use an appliction.properties file to override [the defaults](blob/master/src/main/resources/application.properties). See [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more information.

Example command line configuration, changing the port number:
```bash
java -Xmx3g -jar target/classification-service*.jar --server.port=8081
```

## Building for Debian/Ubuntu Linux
A Debian package can be created using the 'deb' maven profile. 
```bash
mvn clean package -Pdeb
```
