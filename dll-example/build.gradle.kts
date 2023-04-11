plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    @Suppress("DEPRECATION") // :(((
    mingwX86("native") {
        binaries {
            sharedLib {
                baseName = "kotlin-dll-x86"
            }
        }
    }
}
