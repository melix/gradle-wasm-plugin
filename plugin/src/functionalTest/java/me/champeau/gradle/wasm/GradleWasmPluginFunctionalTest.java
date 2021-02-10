/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package me.champeau.gradle.wasm;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

/**
 * A simple functional test for the 'me.champeau.gradle.wasm.greeting' plugin.
 */
public class GradleWasmPluginFunctionalTest {
    @Test public void canRunTask() throws IOException {
        // Setup the test build
        File projectDir = new File("build/functionalTest");
        Files.createDirectories(projectDir.toPath());
        writeString(new File(projectDir, "settings.gradle"), "");
        writeString(new File(projectDir, "build.gradle"),
            "plugins {" +
            "  id('me.champeau.gradle.wasm.greeting')" +
            "}");

        // Run the build
        GradleRunner runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("greet")
                .withProjectDir(projectDir)
                .withDebug(true);
        BuildResult result = runner.build();

        // Verify the result
        assertTrue(result.getOutput().contains("Rust fib(90) = 2880067194370816120"));
        assertTrue(result.getOutput().contains("Java fib(90) = 2880067194370816120"));
        assertTrue(result.getOutput().contains("Precompiled Rust fib(90) = 2880067194370816120"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
