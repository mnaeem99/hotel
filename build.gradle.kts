import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("jacoco")
	id("org.springframework.boot") version "2.5.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("org.sonarqube") version "4.4.1.3373"
	kotlin("jvm") version "1.5.20"
	kotlin("plugin.spring") version "1.5.20"
	kotlin("plugin.jpa") version "1.5.20"
}
group = "com.my.hotel"
version = "0.0.1-SNAPSHOT"


java.sourceCompatibility = JavaVersion.VERSION_11

tasks.getByName<Jar>("jar") {
	enabled = false
}
springBoot {
	mainClass.set("com.my.hotel.server.ServerApplicationKt")
}

repositories {
	mavenCentral()
}

dependencies {
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// API
	implementation("org.springframework.boot:spring-boot-starter-web")

	// AWS
	implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.1000"))
	implementation("com.amazonaws:aws-java-sdk-s3")
	implementation("com.amazonaws:aws-java-sdk-sns:1.12.105")
	implementation("com.amazonaws:aws-java-sdk-secretsmanager:1.12.300")
	implementation("com.amazonaws:aws-java-sdk-ses:1.12.455")
	implementation("org.springframework.cloud:spring-cloud-aws-messaging:2.2.6.RELEASE")


	// GraphQL
	implementation("com.graphql-java-kickstart:graphql-java-tools:11.0.0")
	implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:11.0.0")
	implementation("com.graphql-java:graphql-java-extended-scalars:16.0.1")
	runtimeOnly("com.graphql-java-kickstart:altair-spring-boot-starter:11.0.0")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// DB
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.hibernate:hibernate-spatial:5.5.3.Final")
	implementation("com.bedatadriven:jackson-datatype-jts:2.4")
	implementation("net.postgis:postgis-jdbc:2.5.0")
	implementation("org.liquibase:liquibase-core")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-starter-cache")

	// Utility
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.auth0:java-jwt:3.18.1")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.openapitools:jackson-databind-nullable:0.2.1")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.mockk:mockk:1.10.4")

	//http
	implementation("com.google.api-client:google-api-client:1.32.1")

	//Jose4j
	implementation("org.bitbucket.b_c:jose4j:0.7.9")

	//zxing
	implementation("com.google.zxing:core:3.4.1")
	implementation("com.google.zxing:javase:3.3.0")

	//Quartz
	implementation("javax.transaction:javax.transaction-api:1.3")
	implementation("org.springframework.boot:spring-boot-starter-quartz:2.6.1")
	implementation("org.quartz-scheduler:quartz-jobs:2.3.0")

	//lombok
	compileOnly("org.projectlombok:lombok:1.18.4")

	//thymeleaf
	implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
	implementation("org.thymeleaf:thymeleaf-spring5:3.0.11.RELEASE")

	//firebase
	implementation("com.google.firebase:firebase-admin:9.2.0")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
jacoco {
	toolVersion = "0.8.7"
}
tasks.test {
	finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
	dependsOn(tasks.test) // tests are required to run before generating the report
}
tasks.sonarqube {
	dependsOn(tasks.test)
}
tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
		csv.required.set(false)
		html.required.set(true)
		//html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
	}
}

