Snomed Classification Service
====================================

## Overview
A standalone service for the classification of SNOMED CT ontologies. 

The service takes RF2 files and produces RF2-like results to be further processed by a terminology server. 

## Quick Start
Use Maven to build the executable jar and run:
```bash
mvn clean package
java -Xmx3g -jar target/classification-service*.jar
```
Access the service API documentation at [http://localhost:8089/classification-service](http://localhost:8089/classification-service)

## Configuration
This is a Spring Boot application. Configuration options can be placed after the jar parameter on the command line or use an appliction.properties file to override [the defaults](blob/master/src/main/resources/application.properties). See [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more information.

Example command line configuration, changing the port number:
```bash
java -Xmx3g -jar target/classification-service*.jar --server.port=8081
```
 

## Building for Debian/Ubuntu Linux
The 'deb' maven profile will create a Debian package of this service: 
```bash
mvn clean package -Pdeb
```
