import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*

plugins {
    base
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

val compilerImage by tasks.registering(DockerBuildImage::class) {
    inputs.files(files("src"))
    inputDir.set(file("."))
    dockerFile.set(file("docker/Dockerfile"))
}

val createContainer by tasks.registering(DockerCreateContainer::class) {
    targetImageId(compilerImage.map { it.imageId.get() })
    user.set("gradle")
    workingDir.set("/data")
    cmd.set(listOf("sh", "./build.sh"))
}

val logs by tasks.registering(DockerLogsContainer::class) {
    targetContainerId(createContainer.map { it.containerId.get() })
    since.set(java.util.Date(0))
}

val compileTypeScript by tasks.registering(DockerStartContainer::class) {
    targetContainerId(createContainer.map { it.containerId.get() })
}

val wasmOutputDir = objects.directoryProperty().convention(layout.buildDirectory.dir("wasm"))

val waitForBinaries by tasks.registering(DockerWaitContainer::class) {
    dependsOn(compileTypeScript)
    targetContainerId(createContainer.map { it.containerId.get() })
    finalizedBy(logs)
}

val copyWasmBinaries by tasks.registering(DockerCopyFileFromContainer::class) {
    dependsOn(waitForBinaries)
    targetContainerId(createContainer.map { it.containerId.get() })
    remotePath.set("/data/build")
    hostPath.set(wasmOutputDir.map { it.asFile.absolutePath })
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
            artifact(file("build/wasm/demo_ts.wasm")) {
                builtBy(copyWasmBinaries)
            }
        }
    }
}