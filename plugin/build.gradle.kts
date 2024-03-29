plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.8.21"
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.1.0"
    id("signing")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0")
}

signing {
    useGpgCmd()
}

gradlePlugin {
    plugins {
        create("mugurell.autopublish") {
            id = "io.github.mugurell.autopublish"
            implementationClass = "io.github.mugurell.autopublish.AutopublishPlugin"
            version= "0.9.2"
            website.set("https://github.com/Mugurell/Autopublish")
            vcsUrl.set("https://github.com/Mugurell/Autopublish")
            tags.set(listOf("publish", "maven", "maven-publish", "mavenLocal", "continuousIntegration", "testing"))
            displayName = "STAR - Simple To Autopublish Repositories"
            description = "An anti-bikeshedding (ktlint ™) solution for using local projects in your own!"
        }
    }
}
