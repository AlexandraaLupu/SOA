
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    java
    alias(libs.plugins.jib)
}
springBoot{
    mainClass = "org.example.gateway.GatewayApplication"
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.rest.client)

    testImplementation(libs.spring.boot.starter.test)
}


tasks.test {
    useJUnitPlatform()
}
