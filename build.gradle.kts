import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.3"
	id("io.spring.dependency-management") version "1.0.13.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
	id("org.sonarqube") version "3.4.0.2513"
	id("jacoco")
}

group = "ar.edu.unq.desapp.grupoc"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

apply(plugin = "io.spring.dependency-management")

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.postgresql:postgresql:42.5.0")
	implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.13.4")

	//OpenAPI integration
	implementation("org.springdoc:springdoc-openapi-ui:1.6.11")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.6.11")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation(kotlin("test"))
	testImplementation("io.mockk:mockk:1.12.8")
	testImplementation("com.ninja-squad:springmockk:3.1.1")
}

springBoot{
	mainClass.set("ar.edu.unq.desapp.grupoc.backenddesappapi.BackendDesappApiApplicationKt")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
	dependsOn(tasks.test) // tests are required to run before generating the report
	reports {
		xml.required.set(true)
		xml.outputLocation.set(File("${projectDir}/reports/jacocoXml"))
	}
}
tasks.sonarqube {
	dependsOn(tasks.jacocoTestReport)
}

// SonarQube Extension for code analysis in CI
sonarqube {
	properties {
		property("sonar.projectKey", "amuroBosetti_desapp-grupoc-022022")
		property("sonar.organization", "amurobosetti")
		property("sonar.host.url", "https://sonarcloud.io")
	}
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar"){
    mainClass.set("ar.edu.unq.desapp.grupoc.backenddesappapi.BackendDesappApiApplicationKt")
}

tasks.getByName<Jar>("jar"){
	enabled = false
}



