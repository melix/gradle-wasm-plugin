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
package me.champeau.gradle.wasm.auto;

import me.champeau.gradle.wasm.ann.WasmProtocol;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

@WasmProtocol(
        taskName = "Fibo",
        classpathBinary = "demo_lib",
        functionName = "fibo"
)
public interface FiboIO {
    @Input
    Property<Long> getNumber();
}
