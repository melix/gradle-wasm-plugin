plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

dependencies {
    listOf("linux", "windows", "darwin").forEach { arch ->
        api("org.wasmer:wasmer-jni-amd64-${arch}:0.3.0")
    }
    api("org.graalvm.sdk:graal-sdk:21.0.0.2")
    compileOnly("org.graalvm.truffle:truffle-api:21.0.0.2")
}