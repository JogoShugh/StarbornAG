import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "org.starbornag"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// Spring Boot 3.5 manages Kotlin 1.9 libraries; keep them in step with the Kotlin plugin.
extra["kotlin.version"] = "2.0.21"
extra["springAiVersion"] = "1.1.8"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
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
	implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema-jakarta")
	// Local build of JogoShugh/sse-eventbus (per-subscriber media types); see libs/.
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.starbornag:eventstore")
	implementation("org.postgresql:r2dbc-postgresql")

	implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
	implementation("org.zalando:logbook-core:3.9.0")
	implementation("de.undercouch:actson:2.1.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:r2dbc")
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
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
