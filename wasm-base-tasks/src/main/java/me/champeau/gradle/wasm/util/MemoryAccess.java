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

import java.io.Closeable;

public interface MemoryAccess extends Closeable {
    int getPointer();
    int getSize();
    void deallocate();

    MemoryAccess write(byte[] bytes, int offset, int len);
    MemoryAccess read(byte[] into, int offset, int len);

    default MemoryAccess write(byte[] bytes) {
        return write(bytes, 0, bytes.length);
    }

    default MemoryAccess read(byte[] into) {
        return read(into, 0, into.length);
    }

    default void close() {
        deallocate();
    }
}
