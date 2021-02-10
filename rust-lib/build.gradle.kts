import io.github.liurenjie1024.gradle.rust.CargoBuildTask
import me.champeau.wasm.tasks.WasmGcTask

plugins {
    id("io.github.liurenjie1024.gradle.rust") version "0.1.0"
    id("me.champeau.wasm")
}

val target = "wasm32-unknown-unknown"
val rustLib = "demo-lib"

tasks.withType<CargoBuildTask>().configureEach {
    extraCargoBuildArguments = listOf(
            "--target", target,
            "--release",
            "--target-dir", layout.buildDirectory.get().asFile.absolutePath
    )
}

val wasmGc by tasks.registering(WasmGcTask::class) {
    dependsOn(tasks.named("cargoBuild"))
    inputDirectory.set(wasmOutputDir())
    outputDirectory.set(wasmGcOutputDir())
}

fun wasmOutputDir() = layout.buildDirectory.dir(target)
fun wasmGcOutputDir() = layout.buildDirectory.dir("minified/${target}")

fun embeddedBinary() = wasmGcOutputDir().map {
    it.file("release/${rustLib.replace("-", "_")}.wasm")
}

val wasmElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("wasm-library"))
    }
    outgoing {
        artifacts {
            artifact(embeddedBinary()) {
                builtBy(wasmGc)
            }
        }
    }
}

