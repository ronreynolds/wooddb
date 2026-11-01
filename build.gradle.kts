plugins {
    id("java")
}

group = "com.ronreynolds"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

fun DependencyHandlerScope.lombok() {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

dependencies {
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.yaml) // YAMLFactory
    implementation(libs.jsoup)
    implementation(libs.slf4j.api)

    runtimeOnly(libs.slf4j.simple)

    // Lombok dependencies
    lombok()

    // test dependencies
    testImplementation(libs.assertj.core)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}