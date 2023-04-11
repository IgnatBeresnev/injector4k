plugins {
    kotlin("multiplatform") version "1.8.20"
}

group = "me.beresnev"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    @Suppress("DEPRECATION") // :(((
    mingwX86("native")
}
