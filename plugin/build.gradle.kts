plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("maven-publish")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

gradlePlugin {
    plugins {
        create("AutoPublish") {
            id = "com.autopublish.plugin"
            implementationClass = "tbd"
            version= "1.0"
        }
    }
}
