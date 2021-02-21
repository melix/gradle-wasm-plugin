import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*

plugins {
    base
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

val imageIdOut = layout.buildDirectory.file("imageid.txt")
val imageIdText = providers.fileContents(imageIdOut).asText.forUseAtConfigurationTime()
val containerIdOut = layout.buildDirectory.file("containerid.txt")
val containerIdText = providers.fileContents(containerIdOut).asText.forUseAtConfigurationTime()

val compilerImage by tasks.registering(DockerBuildImage::class) {
    inputs.files(files("src"))
    inputDir.set(file("image"))
    dockerFile.set(file("image/docker/Dockerfile"))
    outputs.file(imageIdOut)

    doLast {
        imageIdOut.get().asFile.writeText(imageId.get())
    }
}

val createContainer by tasks.registering(DockerCreateContainer::class) {
    dependsOn(compilerImage)
    outputs.file(containerIdOut)
    targetImageId(imageIdText)
    user.set("gradle")
    workingDir.set("/data")
    cmd.set(listOf("sh", "./build.sh"))
    outputs.upToDateWhen {
        compilerImage.get().state.upToDate
    }
    doLast {
        containerIdOut.get().asFile.writeText(containerId.get())
    }
}

val compileTypeScript by tasks.registering(DockerStartContainer::class) {
    inputs.file(containerIdOut)
    dependsOn(createContainer)
    targetContainerId(containerIdText)
    outputs.upToDateWhen {
        createContainer.get().state.upToDate
    }
}

val wasmOutputDir = objects.directoryProperty().convention(layout.buildDirectory.dir("wasm"))

val waitForBinaries by tasks.registering(DockerWaitContainer::class) {
    inputs.file(containerIdOut)
    dependsOn(compileTypeScript)
    targetContainerId(containerIdText)
    outputs.upToDateWhen {
        compileTypeScript.get().state.upToDate
    }
}

val copyWasmBinaries by tasks.registering(DockerCopyFileFromContainer::class) {
    inputs.file(containerIdOut)
    dependsOn(waitForBinaries)
    targetContainerId(containerIdText)
    remotePath.set("/data/build")
    hostPath.set(wasmOutputDir.map { it.asFile.absolutePath })
    outputs.upToDateWhen {
        compilerImage.get().state.upToDate
    }
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