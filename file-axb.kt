plugins {
    kotlin("jvm") version "1.9.23" // Use the latest stable Kotlin version
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // For cryptographic hashing (already part of standard JVM)
}

kotlin {
    jvmToolchain(8) // Or 11, 17, depending on your JDK setup
}

application {
    mainClass.set("com.example.blockchain.BlockchainKt") // Adjust if your main file is different
}