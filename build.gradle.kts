plugins {
    java
    application
}

group = "moe.das"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("net.dv8tion:JDA:6.4.2") {
        exclude("opus-java")
    }
}

application {
    mainClass.set("moe.das.honeypot.Main")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}
