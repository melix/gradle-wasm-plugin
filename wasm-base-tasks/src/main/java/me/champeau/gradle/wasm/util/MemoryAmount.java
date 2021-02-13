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

public class MemoryAmount {
    private final int bytes;

    public static MemoryAmount ofBytes(int n) {
        return new MemoryAmount(n);
    }

    public static MemoryAmount ofKiloBytes(int n) {
        return ofBytes(1024 * n);
    }

    public static MemoryAmount ofMegaBytes(int n) {
        return ofKiloBytes(1024 * n);
    }

    public static MemoryAmount ofGigaBytes(int n) {
        return ofMegaBytes(1024 * n);
    }

    private MemoryAmount(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}
