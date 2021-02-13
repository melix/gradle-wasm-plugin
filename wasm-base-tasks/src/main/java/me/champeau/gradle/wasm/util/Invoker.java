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

import org.wasmer.Instance;
import org.wasmer.Memory;
import org.wasmer.exports.Function;

import java.util.concurrent.atomic.AtomicBoolean;

public class Invoker {
    private final static int PAGE_SIZE = 64 * 1024;

    private final Instance instance;
    private final Memory memory;
    private final Function allocator;
    private final Function deallocator;

    public static Invoker forInstance(Instance instance) {
        return new Builder(instance).build();
    }

    public static Builder withInstance(Instance instance) {
        return new Builder(instance);
    }

    private Invoker(Instance instance,
                    String memoryName,
                    String allocator,
                    String deallocator) {
        this.instance = instance;
        this.memory = instance.exports.getMemory(memoryName);
        this.allocator = instance.exports.getFunction(allocator);
        this.deallocator = instance.exports.getFunction(deallocator);
    }

    public MemoryAccess allocate(MemoryAmount amount) {
        int size = amount.getBytes();
        int pointer = apply(allocator, size);
        return new DefaultMemoryAccess(pointer, size);
    }

    public MemoryAccess allocateGrowing(MemoryAmount amount) {
        int size = amount.getBytes();
        memory.grow(1 + (size/ PAGE_SIZE));
        int pointer = apply(allocator, size);
        return new DefaultMemoryAccess(pointer, size);
    }

    @SuppressWarnings("unchecked")
    public <T> T apply(Function f, Object... args) {
        Object[] result = f.apply(args);
        if (result == null || result.length == 0) {
            return null;
        }
        return (T) result[0];
    }

    public <T> T invokeSimple(String function, Object... args) {
        return apply(instance.exports.getFunction(function), args);
    }

    public MemoryAccess invokeToMemory(String function, int size, Object... args) {
        Integer pointer = invokeSimple(function, args);
        return new DefaultMemoryAccess(pointer, size);
    }

    public static class Builder {
        private final Instance instance;

        private String memoryName = "memory";
        private String allocator = "allocate";
        private String deallocator = "deallocate";

        private Builder(Instance instance) {
            this.instance = instance;
        }

        public Builder withMemory(String memory) {
            this.memoryName = memory;
            return this;
        }

        public Builder withAllocator(String allocator) {
            this.allocator = allocator;
            return this;
        }

        public Builder withDeallocator(String deallocator) {
            this.deallocator = deallocator;
            return this;
        }

        public Invoker build() {
            return new Invoker(instance, memoryName, allocator, deallocator);
        }
    }

    private class DefaultMemoryAccess implements MemoryAccess {
        private final AtomicBoolean deallocated = new AtomicBoolean();
        private final int pointer;
        private final int size;

        private DefaultMemoryAccess(int pointer, int size) {
            this.pointer = pointer;
            this.size = size;
        }

        @Override
        public int getPointer() {
            return pointer;
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public void deallocate() {
            if (deallocated.getAndSet(true)) {
                throw new IllegalStateException("Cannot deallocate same memory multiple times");
            }
            apply(deallocator, pointer, size);
        }

        @Override
        public MemoryAccess write(byte[] bytes, int offset, int len) {
            memory.buffer()
                    .position(pointer)
                    .put(bytes, offset, len);
            return this;
        }

        @Override
        public MemoryAccess read(byte[] into, int offset, int len) {
            memory.buffer()
                    .position(pointer)
                    .get(into, offset, len);
            return this;
        }
    }
}
