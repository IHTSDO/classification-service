Snomed Classification Service
====================================

## Overview
An open source standalone REST API for the classification of SNOMED CT ontologies, uses the Snomed OWL Toolkit.

This service is used for the maintenance of the International Edition and will be kept up to date with all description logic enhancements as they are introduced. It is also backward compatible with all historic RF2 releases.

The service takes an RF2 Snapshot archive of a release and an RF2 Delta archive of subsequent changes to produce an RF2 Delta of relationship changes to be further processed by a terminology server. 

## Quick Start
Use Maven to build the executable jar and run:
```bash
mvn clean package
java -Xmx3g -jar target/classification-service*.jar
```
Access the service **API documentation** at [http://localhost:8089/classification-service](http://localhost:8089/classification-service).
The default username and password is _classification_/_classification_.

## Setup
For each SNOMED CT Edition you would like to classify against put an RF2 archive containing the Snapshot into the directory **/store/releases**

When creating a classification using **POST /classifications** the _previousRelease_ parameter should be the filename of one of these archives.

### Configuration options
The default configuration of this Spring Boot application can be found in [application.properties](src/main/resources/application.properties). The defaults can be overridden using command line arguments, for example set a different HTTP port:
```bash
java -Xmx3g -jar target/classification-service*.jar --server.port=8081
```
For other options see [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

The default username and password (classification:classification) can be changed using the _security.user.name_ and _security.user.password_ properties.

## Building for Debian/Ubuntu Linux
A Debian package can be created using the 'deb' maven profile. 
```bash
mvn clean package -Pdeb
```
