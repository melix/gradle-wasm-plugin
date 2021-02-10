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
    implementation("org.wasmer:wasmer-jni-amd64-linux:0.3.0")
    implementation("org.wasmer:wasmer-jni-amd64-windows:0.3.0")
    implementation("org.wasmer:wasmer-jni-amd64-darwin:0.3.0")
    testImplementation("junit:junit:4.13.1")
}
