plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.8.21"
    id("maven-publish")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0")
}

gradlePlugin {
    plugins {
        create("AutoPublish") {
            id = "com.autopublish.plugin"
            implementationClass = "com.autopublish.AutopublishPlugin"
            version= "0.9-snapshot"
        }
    }
}
