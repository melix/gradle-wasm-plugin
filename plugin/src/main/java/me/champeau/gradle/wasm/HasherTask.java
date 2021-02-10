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

import me.champeau.gradle.wasm.tasks.AbstractSimpleWasmTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

@CacheableTask
public abstract class HasherTask extends AbstractSimpleWasmTask {
    @InputFile
    @PathSensitive(PathSensitivity.NAME_ONLY)
    abstract RegularFileProperty getInputFile();

    @OutputFile
    abstract RegularFileProperty getOutputFile();

    public HasherTask() {
        getFunctionName().set("md5");
        fromClasspathLib("demo_lib");
        getOutputFile().convention(
                getProject().getLayout()
                        .getBuildDirectory()
                        .file(getProject().provider(this::outputFileName))
        );
    }

    private String outputFileName() {
        return "hashes/" + getInputFile().getAsFile().get().getName() + ".md5";
    }

    @TaskAction
    void execute() {
        File inputFile = getInputFile().getAsFile().get();
        File outputFile = getOutputFile().getAsFile().get();
        outputFile.getParentFile().mkdirs();

        // insert call to wasm library here
        callFunction(inputFile, outputFile);
    }
}
