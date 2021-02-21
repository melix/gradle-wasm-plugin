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
import me.champeau.gradle.wasm.util.Fibo;
import me.champeau.gradle.wasm.util.Measure;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class Greeter extends AbstractSimpleWasmTask {

    @Input
    abstract Property<Long> getNumber();

    public Greeter() {
        getFunctionName().set("fibo");
        fromClasspathLib("demo_lib");
        getNumber().convention(
                90L
        );
    }

    @TaskAction
    void execute() {
        Long n = getNumber().get();
        Measure.operation("Using memoized fib written in Rust and compiled to WASM", () ->{
            Long calc = callFunction(n);
            System.out.println("Rust fib(" + n + ") = " + calc);
        });
        Measure.operation("Using memoized fib written in Java", () ->{
            long calc = Fibo.of(n);
            System.out.println("Java fib(" + n + ") = " + calc);
        });
        withWasmRuntime(invoker -> {
            Measure.operation("With external WASM to native compilation", () -> {
                Long calc = invoker.invokeSimple(getFunctionName().get(), n);
                System.out.println("Precompiled Rust fib(" + n + ") = " + calc);
            });
            return null;
        });
    }
}
