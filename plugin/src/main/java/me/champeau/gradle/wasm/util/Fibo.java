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
package me.champeau.gradle.wasm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Fibo {
    private final Function<Long, Long> memoizedFib = Memoizer.of((Long x) -> {
        if (x == 0) {
            return 0L;
        }
        if (x == 1) {
            return 1L;
        }
        return this.memoizedFib.apply(x - 1) + this.memoizedFib.apply(x - 2);
    });

    public static long of(long n) {
        return new Fibo().fib(n);
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
            return input -> {
                if (cache.containsKey(input)) {
                    return cache.get(input);
                }
                R res = function.apply(input);
                cache.put(input, res);
                return res;
            };
        }

    }
}
