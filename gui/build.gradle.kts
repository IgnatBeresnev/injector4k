import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable

val os = checkNotNull(OperatingSystem.current()) { "Unable to determine OS" }
val isRunningInIde: Boolean = System.getProperty("idea.active") == "true"

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()

    @Suppress("DEPRECATION")
    jcenter() // for kotlin-libui
}

kotlin {
    @Suppress("DEPRECATION") // :(((
    mingwX86("native") {
        binaries {
            executable(listOf(RELEASE)) {
                windowsResources("${project.projectDir}/src/nativeMain/resources/injector.rc")
                baseName = "injector4k-x86"
                entryPoint = "me.beresnev.injector.ui.launchGUI"
            }
        }
    }

    val nativeMain by sourceSets.getting {
        dependencies {
            implementation("com.github.msink:libui:0.1.8")
            implementation(project(":injector"))
        }
    }
}

// copied from kotlin-libui samples with some corrections to make it work with this project
// https://github.com/msink/kotlin-libui/blob/0.1.8/samples/build.gradle.kts
fun Executable.windowsResources(rcFileName: String) {
    val taskName = linkTaskName.replaceFirst("link", "windres")
    val inFile = File(rcFileName)
    val outFile = buildDir.resolve("processedResources/$taskName.res")

    val windresTask = tasks.create<Exec>(taskName) {
        val konanDataDir = System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan"
        val toolchainBinDir = when (target.konanTarget.architecture.bitness) {
            32 -> File("$konanDataDir/dependencies/msys2-mingw-w64-i686-2/bin").invariantSeparatorsPath
            64 -> File("$konanDataDir/dependencies/msys2-mingw-w64-x86_64-2/bin").invariantSeparatorsPath
            else -> error("Unsupported architecture")
        }

        inputs.file(inFile)
        outputs.file(outFile)
        commandLine("$toolchainBinDir/windres", inFile, "-D_${buildType.name}", "-O", "coff", "-o", outFile)
        environment("PATH", "$toolchainBinDir;${System.getenv("PATH")}")

        dependsOn(compilation.compileKotlinTask)
    }

    linkTask.dependsOn(windresTask)
    linkerOpts(outFile.toString())
}
