plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "8.3.1"
}

group = "dev.soncresityindustries"
version = "1.0.0"

application {
    mainClass.set("dev.soncresityindustries.StatusBot.Bot")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:6.1.1")
    implementation("org.yaml:snakeyaml:2.5")
    implementation("ch.qos.logback:logback-classic:1.5.19")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    sourceCompatibility = "21"
}