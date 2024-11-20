import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "org.starbornag"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

//extra["springAiVersion"] = "1.0.0-M2"
extra["springAiVersion"] = "1.0.0-20241008.115115-715"
repositories {
	mavenCentral()
	maven {
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
	}
	maven {
		url = uri("https://repo.spring.io/milestone")
	}
	maven {
		url = uri("https://repo.spring.io/snapshot")
	}
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.github.marlonlom:timeago:4.0.0")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("com.squareup.moshi:moshi:1.15.1")
	implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("com.github.marlonlom:timeago")
	implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema-jakarta")
	//implementation("ch.rasc:sse-eventbus:2.0.0")
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
	implementation("com.eventstore:db-client-java:5.4.1")


	val kotlinxHtmlVersion = "0.11.0"
	// include for JVM target
	implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")

	val logbookVersion = "3.9.0"
	implementation("org.zalando:logbook-core:$logbookVersion")

	implementation("de.undercouch:actson:2.1.0")

//	// include for JS target
//	implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.10.1")
//
//	// include for Common module
//	implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:r2dbc")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.postgresql:postgresql")
	implementation("org.postgresql:r2dbc-postgresql")
	implementation("org.springframework:spring-jdbc")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

springBoot {
	mainClass.set("org.starbornag.api.ApiApplicationKt")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	layered {
		enabled = false
	}
}

tasks.getByName<Jar>("jar") {
	manifest {
		attributes(
			mapOf(
				"Main-Class" to "org.starbornag.api.ApiApplicationKt" // Main class
			)
		)
	}
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) // Or just "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
	inputs.dir(project.extra["snippetsDir"]!!)
	dependsOn(tasks.test)
}
