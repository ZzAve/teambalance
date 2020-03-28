# teambalance
Personal project to monitor team expenses on beer and brown fruit




## Setup

Technology

- Spring boot
- Spring data (JPA)
- Spring security
- Kotlin
- React
- Google App Engine (appengine.yaml)
- Postgres database in GCP

## Deploying to PRO:

[Find reference docs here](https://cloud.google.com/appengine/docs/standard/java/tools/uploadinganapp)


```bash
./mvnw clean package appengine:deploy
```

## Connect to the database
Teambalance makes use of a Postgres db that lives in GCP.

By having added the following dependency  
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-sql-postgresql</artifactId>
</dependency>
```

And a little bit of configuration
```yaml
spring:
  datasource:
    username: <username>
    password: <password>

spring.cloud.gcp.sql:
  instance-connection-name: <db-instance>
  database-name: <db-name>
```
the connection to my set up database works like a charm. [You can check the approach followed here](https://github.com/spring-cloud/spring-cloud-gcp/tree/master/spring-cloud-gcp-samples/spring-cloud-gcp-data-jpa-sample) 

To be able to run this locally, one should make sure to be connected to gcloud, and having [credentials for a service account
for a service account](https://cloud.google.com/sdk/gcloud/reference/auth/application-default/login)

## TODO:
### Must have:
 - ~~Ability to contribute to team balance~~
 - ~~View latest contribution to team balance~~
 - ~~Training overview including player availability~~
 - Match overview including player availability
 - Training admin screen (add/change/remove trainings)
 - Match admin screen (add/change/remove matches)
 - Get back polling mechanism when back-end is still down.
 - Ensure training endpoint are also protected
 - Use 'proper' authentication mechanism.
 
### Should have:
 - Debounce was introduced to ensure every API call takes at least 500 ms (for UX purposes). This only works if a call is successful. Should also work for unsuccessful ones.
 - Github actions, used to deploy to Google cloud on every merge to master 
 - Stg env for testing purposes (use a different application version, but don't take all traffic ?)
 
### Could have
 - Availabilities and agenda for non training/match events (like team uitje)
 - Upload receipts and tie them to payments
 - Stats on team balanc contributors
 - Link to Nevobo site with competition
 - Integration with Nevobo: Link to Nevobo team 'API': https://api.nevobo.nl/export/team/CKL7W0D/heren/1/programma.rss
 
### Won't have:
 - tbd
 