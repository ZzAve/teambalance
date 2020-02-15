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
 Must have:
 - Training overview including player availability
 - Match overview including player availability
 
 
 Nice to have:
 - Link to Nevobo team 'API': https://api.nevobo.nl/export/team/CKL7W0D/heren/1/programma.rss
 