plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    mingwX86("native")
}
