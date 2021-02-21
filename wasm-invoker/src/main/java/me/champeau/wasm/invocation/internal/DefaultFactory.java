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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.ByteSequence;
import org.wasmer.Instance;

public class DefaultFactory implements Invoker.Factory {
    private final byte[] binary;

    private String memoryName = "memory";
    private String allocator = "allocate";
    private String deallocator = "deallocate";

    public DefaultFactory(byte[] binary) {
        this.binary = binary;
    }

    public Invoker.Factory withMemory(String memory) {
        this.memoryName = memory;
        return this;
    }

    public Invoker.Factory withAllocator(String allocator) {
        this.allocator = allocator;
        return this;
    }

    public Invoker.Factory withDeallocator(String deallocator) {
        this.deallocator = deallocator;
        return this;
    }

    public Invoker build() {
        try {
            Source.Builder sourceBuilder = Source.newBuilder("wasm",
                    ByteSequence.create(binary),
                    "task");
            Source source = sourceBuilder.build();
            Context.Builder contextBuilder = Context.newBuilder("wasm");
            Context context = contextBuilder.build();
            context.eval(source);

            return new GraalVMInvoker(context.getBindings("wasm").getMember("main"), memoryName, allocator, deallocator);
        } catch (Exception ex) {
            System.err.println("GraalVM runtime not available");
            return new WasmerInvoker(new Instance(binary), memoryName, allocator, deallocator);
        }
    }
}
