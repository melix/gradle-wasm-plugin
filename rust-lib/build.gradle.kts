import io.github.liurenjie1024.gradle.rust.CargoBuildTask

plugins {
    id("io.github.liurenjie1024.gradle.rust") version "0.1.0"
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

fun wasmBinary() = layout.buildDirectory
        .file(provider { "${target}/release/${rustLib.replace("-", "_")}.wasm" })

val wasmElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("wasm-library"))
    }
    outgoing {
        artifacts {
            artifact(wasmBinary()) {
                builtBy(tasks.named("cargoBuild"))
            }
        }
    }
}

