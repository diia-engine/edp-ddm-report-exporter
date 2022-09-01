# report-exporter

This service provides web API for simplified exporting dashboards with all related queries from Redash.
It also provides endpoints to browse all available dashboards in Redash

### Related components:

* Redash instance as a platform for dashboards
* OpenShift to store API key as a secret
* `report-publisher` imports dashboards and queries

### Local development:
###### Prerequisites:

* Redash instance is configured and running

###### Configuration:
* `src/main/resources/application-local.yaml` for local development with actual property values (redash.url - URL of Redash instance)

###### Steps:

1. (Optional) Package application into jar file with `mvn clean package`
2. Add `--spring.profiles.active=local` to application run parameters
3. Run application with your favourite IDE or via `java -jar ...` with jar file, created above

### License
report-exporter is Open Source software released under the Apache 2.0 license.