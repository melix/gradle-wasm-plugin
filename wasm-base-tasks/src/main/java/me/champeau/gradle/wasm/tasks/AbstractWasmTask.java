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
package me.champeau.gradle.wasm.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.wasmer.Instance;

import java.io.InputStream;
import java.util.function.Function;

public abstract class AbstractWasmTask extends DefaultTask {
    @Internal
    protected abstract Property<byte[]> getWasmBinary();

    protected void fromClasspathLib(String name) {
        String lib = name + ".wasm";
        getWasmBinary().set(getProject().getProviders().provider(() -> {
            ClassLoader classLoader = this.getClass().getClassLoader();
            InputStream wasmLib = classLoader.getResourceAsStream(lib);
            if (wasmLib != null) {
                return wasmLib.readAllBytes();
            }
            throw new InvalidUserCodeException("Cannot find '" + lib + "' on classpath");
        }));
    }

    protected final <T> T withWasmRuntime(Function<Instance, T> fun) {
        byte[] bytecode = getWasmBinary().get();
        Instance instance = new Instance(bytecode);
        try {
            return fun.apply(instance);
        } finally {
            instance.close();
        }
    }
}
