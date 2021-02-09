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
import org.gradle.api.tasks.TaskAction;

public abstract class Greeter extends AbstractSimpleWasmTask {
    public Greeter() {
        getFunctionName().set("sum");
        fromClasspathLib("demo_lib");
    }

    @TaskAction
    void execute() {
        int sum = callFunction(13, 29);
        System.out.println("Sum is " + sum);
    }
}
