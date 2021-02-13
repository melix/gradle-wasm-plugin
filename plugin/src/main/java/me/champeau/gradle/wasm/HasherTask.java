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
import me.champeau.wasmer.util.Invoker;
import me.champeau.wasmer.util.MemoryAccess;
import me.champeau.wasmer.util.MemoryAmount;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CacheableTask
public abstract class HasherTask extends AbstractWasmTask {

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
            Invoker invoker = Invoker.forInstance(instance);
            int len = input.length;
            try (MemoryAccess fileContents = invoker.allocateGrowing(MemoryAmount.ofBytes(len))) {
                fileContents.write(input);
                MemoryAccess result = invoker.invokeToMemory("process", 16, fileContents.getPointer(), len);
                byte[] hash = new byte[16];
                result.read(hash);
                String hexHash = toHexString(hash);
                try (PrintWriter wrt = new PrintWriter(new FileWriter(outputFile))) {
                    wrt.println(hexHash);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("hash from Rust is " + hexHash);
                try {
                    hash = MessageDigest.getInstance("MD5").digest(input);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                System.out.println("hash from Java is " + toHexString(hash));
            }
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
