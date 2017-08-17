Snomed Classification Service
====================================

Overview
-------
This service works alongside the terminology server to provide further backend functionality to the authoring environment.

### Build

### Build Debian Package
```bash
mvn clean package -Pdeb
```

### Run locally
```bash
java -Xmx3g -jar target/classification-service-1.0-SNAPSHOT.jar --spring.config.location=src/main/resources/application.properties
```
Note that the port number used is supplied by the application.config file

