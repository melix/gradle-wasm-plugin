plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

val wasmLibraries by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("wasm-library"))
    }
}

dependencies {
    annotationProcessor(project(":wasm-gradle-processor"))
    compileOnly(project(":wasm-gradle-annotations"))

    wasmLibraries(project(":rust-lib")) {
        because("This demo uses a library built with Rust")
    }
    wasmLibraries(project(":assemblyscript-lib")) {
        because("This demo uses a library built with AssemblyScript")
    }
    implementation(project(":wasm-base-tasks")) {
        because("Task implementation is a WASM library")
    }
    testImplementation("junit:junit:4.13.1")
}

tasks.processResources {
    from(wasmLibraries)
}

gradlePlugin {
    // Define the plugin
    val greeting by plugins.creating {
        id = "me.champeau.gradle.wasm.greeting"
        implementationClass = "me.champeau.gradle.wasm.GradleWasmPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.register<Test>("graalFunctionalTest") {
    val functionalTest = tasks.getByName<Test>("functionalTest")
    classpath = functionalTest.classpath
    testClassesDirs = functionalTest.testClassesDirs
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.matching("GraalVM"))
    })
    jvmArgs(listOf("--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED"))
}