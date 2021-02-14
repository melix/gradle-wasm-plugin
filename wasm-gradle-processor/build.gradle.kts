plugins {
    `java-library`
}

dependencies {
    implementation(project(":wasm-gradle-annotations"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
