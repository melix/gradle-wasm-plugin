plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    compileOnly(gradleApi())
    api(project(":wasm-invoker"))
    testImplementation("junit:junit:4.13.1")
}
