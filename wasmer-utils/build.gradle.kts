plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    listOf("linux", "windows", "darwin").forEach { arch ->
        api("org.wasmer:wasmer-jni-amd64-${arch}:0.3.0")
    }
}