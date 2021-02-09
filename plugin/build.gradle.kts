plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":wasm-base-tasks")) {
        because("Task implementation is a WASM library")
    }
    testImplementation("junit:junit:4.13.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
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
    wasmLibraries(project(":rust-lib")) {
        because("This demo uses a library built with Rust")
    }
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

tasks.register("dump") {
    doLast {
        configurations["functionalTestRuntimeClasspath"].files.forEach {
            println(it)
        }
    }
}