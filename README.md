## Gradle WASM tasks

This project is a proof-of-concept of Gradle tasks implemented via WASM libraries.
In short, this allows writing a Gradle task implementation in any language which supports compiling to [WebAssembly](https://webassembly.org/).

The project consists of different modules:

The core modules are:

- `wasm-base-tasks` is a support library providing the base tasks which support WASM binaries
- `wasmer-utils` is a helper library which makes it more convenient to call wasmer Java, in particular handling memory

An annotation processor consists of 2 modules:

- `wasm-gradle-annotations` defines the annotations of the processor
- `wasm-gradle-processor` is an annotation processor which automatically generates a Gradle task implementation from a "protocol"

Then the following modules are there for demo purposes:

- `rust-lib` is a sample library written in Rust and compiling to WASM
- `plugin` is a plugin which loads the `rust-lib` library and uses it in a task

Because of the rough integration with Rust, this project assumes that you have `rust` installed with the proper wasm bindings:

```
$ rustup target add wasm32-unknown-unknown
% cargo install wasm-gc
```

### Implementation details

The `wasm-base-tasks` project defines a class called `me.champeau.gradle.wasm.tasks.AbstractWasmTask` which allows declaring a wasm binary as an input and provides a convenience for calling the binary.
It uses [wasmer-java](https://github.com/wasmerio/wasmer-java) to execute the code.

### Trying out

To test this, execute `./gradlew functionalTest`