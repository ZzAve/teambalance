# teambalance
Personal project to monitor team expenses on beer and brown fruit

## What it does
Team balance started off a hobby project and technology explorer (see setup). Its goal is to allow a group of people 
that go out together often to have a kind of shared wallet or 'pot' in Dutch. With everybody being able to chip in and 
see what transactions went out, it allows the bills to be paid from a single bank account.

As it is becoming more uncommon to use cash, and bars and clubs allow card payments more often,
 the need for a digital money pool is becoming more and more apparent.


### Parts
Team balance has 2 main parts:

- BankAPI that has a connection with bunqAPI. To gain insights in transactions on the team's money pool
- EventsAPI to allow teammembers to register availability for upcoming trainings and matches. Admins can also create events

The APIs are exposed through a reactive front-end that includes an authentication flow as well to prevent nosy neighbours
from peeking.

## Comparable solutions:
(this list is probably never complete)

1. [Paypal's money pools](https://www.paypal.com/uk/webapps/mpp/money-pools):
    1.  More targeted towards reaching a certain goals (in terms of $), to be able to buy something.
1. [Tikkie (url will follw)](#)
    1. Tikkie allows you to pay people upon request. Unfortunately there's no way of seeing how much money is available.
    1. Tikkie's concept of payment requests is used by team balance. [bunq.me](https://bunq.me) is a similar concept where
    one can own a page a allow people to pay whatever they want, and include a message.


## Dev

### Setup

Technology

- Spring boot
- Spring data (JPA)
- Spring security
- Kotlin
- React
- Google App Engine (appengine.yaml)
- Postgres database in GCP


### Deploying to PRO:

[Find reference docs here](https://cloud.google.com/appengine/docs/standard/java/tools/uploadinganapp)


```bash
./mvnw clean package appengine:deploy
```

### Connect to the database
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
 - ~~Use polling mechanism when back-end is still down.~~
 - Ensure training endpoint are also protected
 - Use 'proper' authentication mechanism.
 
### Should have:
 - ~~Debounce was introduced to ensure every API call takes at least 500 ms (for UX purposes). This only works if a call is successful. Should also work for unsuccessful ones.~~
 - Github actions, used to deploy to Google cloud on every merge to master 
 - Stg env for testing purposes (use a different application version, but don't take all traffic ?)
 
### Could have
 - Availabilities and agenda for non training/match events (like team uitje)
 - Upload receipts and tie them to payments
 - Stats on team balanc contributors
 - Link to Nevobo site with competition
 - Integration with Nevobo: Link to Nevobo team 'API': https://api.nevobo.nl/export/team/CKL7W0D/heren/1/programma.rss
 - A setup that makes it reusable for different teams as well.
 
### Won't have:
 - Integration with CMS systems for customisation purposes
 