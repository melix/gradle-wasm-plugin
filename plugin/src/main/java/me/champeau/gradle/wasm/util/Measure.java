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

import java.util.concurrent.TimeUnit;

public class Measure {
    public static void operation(String label, Runnable r) {
        System.out.println(label);
        long sd = System.nanoTime();
        try {
            r.run();
        } finally {
            long dur = System.nanoTime() - sd;
            if (dur > 1000) {
                System.out.println("Took " + TimeUnit.NANOSECONDS.toMillis(dur) + "ms");
            } else {
                System.out.println("Took " + dur + "ns");
            }
        }
    }
}
