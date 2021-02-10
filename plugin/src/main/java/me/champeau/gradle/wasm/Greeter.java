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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class Greeter extends AbstractSimpleWasmTask {
    private final Function<Long, Long> memoizedFib = Memoizer.of((Long x) -> {
        if (x == 0) {
            return 0L;
        }
        if (x == 1) {
            return 1L;
        }
        return this.memoizedFib.apply(x - 1) + this.memoizedFib.apply(x - 2);
    });

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
        measure("Using memoized fib written in Rust and compiled to WASM", () ->{
            long calc = callFunction(n);
            System.out.println("Rust fib(" + n + ") = " + calc);
        });
        measure("Using memoized fib written in Java", () ->{
            long calc = callFunction(n);
            System.out.println("Java fib(" + n + ") = " + calc);
        });
        withWasmRuntime(instance -> {
            measure("With external WASM to native compilation", () -> {
                Object[] result = apply(instance, n);
                long calc = (Long) result[0];
                System.out.println("Precompiled Rust fib(" + n + ") = " + calc);
            });
            return null;
        });
    }

    private void measure(String label, Runnable r) {
        System.out.println(label);
        long sd = System.nanoTime();
        try {
            r.run();
        } finally {
            long dur = System.nanoTime() - sd;
            System.out.println("Took " + TimeUnit.NANOSECONDS.toMillis(dur) + "ms");
        }
    }

    private long fib(long n) {
        return memoizedFib.apply(n);
    }

    private static class Memoizer<T, R> {
        private final Map<T, R> cache = new HashMap<>();

        private Memoizer() {
        }

        public static <T, R> Function<T, R> of(Function<T, R> function) {
            return new Memoizer<T, R>().memoize(function);
        }

        private Function<T, R> memoize(Function<T, R> function) {
            return input -> cache.computeIfAbsent(input, function);
        }

    }
}
