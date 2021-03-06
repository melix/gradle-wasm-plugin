/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package me.champeau.gradle.wasm;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class GradleWasmPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().register("greet", Greeter.class);
        project.getTasks().register("md5", HasherTask.class);
    }
}
