# MyHotel Server

## Purpose

The purpose of the MyHotel is to provide messaging with hotels and friends, hotel recommendation, hotel loyalty programs, 
hotel loyalty gifts, and other services to users around the world.

## Requirements

The fully fledged server uses the following:

* Work with JVM 11
* Language: Kotlin (version: 1.5.2)
* Build Tool: Gradle
* Framework: SpringBoot (version: 2.5.2)
* Editor: IntelliJ
* API: GraphQL (Altair or Postman)
* Database: PostgreSQL (pgAdmin)
* Deployment: Docker & AWS

## Dependencies
There are a number of third-party dependencies used in the project. Browse the Gradle build.gradle.kts file for details of libraries and versions used.


The application exposes health endpoint (http://localhost:8087/healthcheck).

## Setup

To check out the project and build it from source, do the following:

git clone https://github.com/mnaeem99/hotel.git


## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8087` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8087/healthcheck
```

You should get a response similar to this:

```
  ok
```


## Supported AWS integrations

| AWS Service     |
|-----------------|
| S3              |                   
| SNS             |                   
| SES             |
| Secrets Manager |
| SQS             |
| RDS             |
| ECR             |


