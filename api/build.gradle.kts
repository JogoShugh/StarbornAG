import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant
import java.time.temporal.ChronoUnit

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
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
// Cucumber 7.34 needs JUnit Platform 1.14; Spring Boot 3.5 manages JUnit 5.12.
extra["junit-jupiter.version"] = "5.14.4"

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

	// bdd-gates: Cucumber on the JUnit Platform (strict: undefined steps fail), Konsist architecture tests.
	testImplementation("org.junit.platform:junit-platform-suite")
	testImplementation("io.cucumber:cucumber-java:7.34.6")
	testImplementation("io.cucumber:cucumber-junit-platform-engine:7.34.6")
	testImplementation("io.cucumber:cucumber-picocontainer:7.34.6")
	testImplementation("com.lemonappdev:konsist:0.17.3")
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

// bdd-gates: records "BDD_RUN PASS|FAIL" in the repository's .claude/tdd-events.log after every
// test run, including failed ones. finalizedBy, not doLast: Gradle skips doLast when tests fail.
// Gradle 9 writes one JUnit XML per feature file, so this reads Cucumber's own JSON report.
val logBddRun = tasks.register("logBddRun") {
	group = "verification"
	description = "Appends a BDD_RUN fact to .claude/tdd-events.log for the pre-commit TDD-ordering check."
	val reportFile = layout.buildDirectory.file("reports/cucumber/report.json")
	val logFile = rootDir.resolve("../.claude/tdd-events.log")
	doLast {
		val timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS)
		logFile.parentFile.mkdirs()
		val json = reportFile.get().asFile.takeIf { it.exists() }?.readText()
		if (json == null) {
			// No report means compilation or discovery failed: still red.
			logFile.appendText("$timestamp BDD_RUN FAIL reason=no-results\n")
			return@doLast
		}
		fun count(pattern: String) = Regex(pattern).findAll(json).count()
		val scenarios = count("""\"type"\s*:\s*"scenario"""")
		// Undefined or pending steps count as red: a new scenario without step code has not passed.
		val notPassed = count("""\"status"\s*:\s*"(failed|undefined|pending|ambiguous)"""")
		val status = if (notPassed == 0) "PASS" else "FAIL"
		logFile.appendText("$timestamp BDD_RUN $status scenarios=$scenarios notPassedSteps=$notPassed\n")
	}
}

tasks.test {
	finalizedBy(logBddRun)
}

// bdd-gates: complexity gate at Detekt's own default thresholds. The baseline records the
// findings in code written before the gates existed; new code gets no such allowance.
detekt {
	buildUponDefaultConfig = true
	baseline = file("detekt-baseline.xml")
}

tasks.check {
	dependsOn(tasks.named("detekt"))
}
