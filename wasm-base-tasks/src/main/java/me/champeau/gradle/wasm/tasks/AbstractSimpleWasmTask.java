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

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.wasmer.Instance;

public abstract class AbstractSimpleWasmTask extends AbstractWasmTask {
    @Internal
    protected abstract Property<String> getFunctionName();

    @SuppressWarnings("unchecked")
    protected final <T> T callFunction(Object... args) {
        return (T) withWasmRuntime(instance -> apply(instance, args))[0];
    }

    protected Object[] apply(Instance instance, Object... args) {
        return findFunction(instance, getFunctionName().get()).apply(args);
    }

}
