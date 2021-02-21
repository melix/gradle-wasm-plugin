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
package me.champeau.wasm.invocation.internal;

import me.champeau.wasm.invocation.Invoker;
import me.champeau.wasm.invocation.MemoryAccess;
import me.champeau.wasm.invocation.MemoryAmount;
import org.graalvm.polyglot.Value;

import java.util.concurrent.atomic.AtomicBoolean;

class GraalVMInvoker implements Invoker {
    private final static int PAGE_SIZE = 64 * 1024;

    private final Value context;
    private final Value memory;
    private final String allocator;
    private final String deallocator;

    GraalVMInvoker(Value context,
                   String memoryName,
                   String allocator,
                   String deallocator) {
        this.context = context;
        this.memory = context.getMember(memoryName);
        this.allocator = allocator;
        this.deallocator = deallocator;
    }

    public MemoryAccess allocate(MemoryAmount amount) {
        int size = amount.getBytes();
        int pointer = apply(allocator, size);
        return new DefaultMemoryAccess(pointer, size);
    }

    public MemoryAccess allocateGrowing(MemoryAmount amount) {
        int size = amount.getBytes();
        // TODO: call grow?
        int pointer = apply(allocator, size);
        return new DefaultMemoryAccess(pointer, size);
    }

    @SuppressWarnings("unchecked")
    private <T> T apply(String fName, Object... args) {
        return (T) context.getMember(fName).execute(args).as(Object.class);
    }

    public <T> T invokeSimple(String function, Object... args) {
        return apply(function, args);
    }

    public MemoryAccess invokeToMemory(String function, int size, Object... args) {
        Integer pointer = invokeSimple(function, args);
        return new DefaultMemoryAccess(pointer, size);
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
            for (int i = 0; i < len; i++) {
                memory.setArrayElement(pointer + i, bytes[offset + i]);
            }
            return this;
        }

        @Override
        public MemoryAccess read(byte[] into, int offset, int len) {
            for (int i = 0; i < len; i++) {
                into[offset + i] = memory.getArrayElement(pointer + i).as(Integer.class).byteValue();
            }
            return this;
        }
    }

}
