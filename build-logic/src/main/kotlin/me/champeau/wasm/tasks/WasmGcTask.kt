package me.champeau.wasm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

@CacheableTask
abstract class WasmGcTask: DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun minimize() {
        inputDirectory.asFileTree.visit(::processFile)
    }

    private
    fun processFile(details: FileVisitDetails) {
        val inputFile = details.file
        if (details.isDirectory && inputFile.list()!!.any { it.endsWith(".wasm") }) {
            outputDirectory.dir(details.relativePath.pathString).get().asFile.mkdirs()
        } else if (inputFile.name.endsWith(".wasm")) {
            val targetFile = outputDirectory.file(details.relativePath.pathString).get().asFile
            val execResult = try {
                execOperations.exec {
                    commandLine(listOf(
                            "wasm-gc",
                            inputFile.absolutePath,
                            targetFile.absolutePath
                    ))
                }.exitValue
            } catch (e: Exception) {
                logger.warn(e.message)
                -1
            }
            if (execResult != 0) {
                logger.warn("Unable to minify $inputFile")
                targetFile.writeBytes(inputFile.readBytes())
            }
        }
    }
}