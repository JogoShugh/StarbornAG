import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	//kotlin("plugin.serialization") version "2.0.10"
}

group = "org.starbornag"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")


dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.github.marlonlom:timeago:4.0.0")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	//implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
	implementation("com.squareup.moshi:moshi:1.15.1")
	implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("com.github.marlonlom:timeago")
	implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema-jakarta")
	implementation("ch.rasc:sse-eventbus:2.0.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
}

springBoot {
	mainClass.set("org.starbornag.api.ApiApplicationKt")
}

//tasks.withType<Jar> {
//	manifest {
//		attributes["Main-Class"] = "org.starbornag.api.ApiApplicationKt"
//	}
//}

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
