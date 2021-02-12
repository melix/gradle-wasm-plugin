/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.gradle.wasm;

import me.champeau.gradle.wasm.tasks.AbstractWasmTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.wasmer.Memory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CacheableTask
public abstract class HasherTask extends AbstractWasmTask {
    private final static int PAGE_SIZE = 64 * 1024;

    @InputFile
    @PathSensitive(PathSensitivity.NAME_ONLY)
    abstract RegularFileProperty getInputFile();

    @OutputFile
    abstract RegularFileProperty getOutputFile();

    public HasherTask() {
        fromClasspathLib("demo_lib");
        getOutputFile().convention(
                getProject().getLayout()
                        .getBuildDirectory()
                        .file(getProject().provider(this::outputFileName))
        );
        getInputFile().fileValue(new File("build.gradle"));
    }

    private String outputFileName() {
        return "hashes/" + getInputFile().getAsFile().get().getName() + ".md5";
    }

    @TaskAction
    void execute() {
        withWasmRuntime(instance -> {
            File inputFile = getInputFile().getAsFile().get();
            File outputFile = getOutputFile().getAsFile().get();
            outputFile.getParentFile().mkdirs();
            byte[] input = readFile(inputFile);
            if (input == null) {
                return null;
            }
            Memory memory = instance.exports.getMemory("memory");
            int len = input.length;
            System.out.println("Input file len = " + len);
            memory.grow(1 + (len / PAGE_SIZE));

            // Allocate memory for the subject, and get a pointer to it.
            Integer input_pointer = apply(instance, "allocate", len);

            ByteBuffer buffer = memory.buffer();
            buffer.mark();
            buffer.position(input_pointer);
            buffer.put(input);

            input_pointer = apply(instance, "process", input_pointer, len);
            byte[] hash = new byte[16];
            buffer.reset();
            buffer.position(input_pointer);
            buffer.get(hash);

            System.out.println("hash from Rust is " + toHexString(hash));
            try {
                hash = MessageDigest.getInstance("MD5").digest(input);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            System.out.println("hash from Java is " + toHexString(hash));
            return null;
        });
    }

    private static byte[] readFile(File inputFile) {
        byte[] input;
        try {
            input = Files.readAllBytes(inputFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return input;
    }

    private static String toHexString(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }
}
