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
package me.champeau.gradle.wasm.proc;

import me.champeau.gradle.wasm.ann.WasmProtocol;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("me.champeau.gradle.wasm.ann.WasmProtocol")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WasmGradleProcessor extends AbstractProcessor {
    private final static List<String> TASK_IMPORTS = Collections.unmodifiableList(Arrays.asList(
            "org.gradle.api.tasks.TaskAction",
            "me.champeau.wasm.invocation.Invoker",
            "me.champeau.gradle.wasm.tasks.AbstractSimpleWasmTask"
    ));

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(TypeElement.class::isInstance)
                    .map(TypeElement.class::cast)
                    .map(e -> new ProcessingUnit(roundEnv, processingEnv, e))
                    .forEach(ProcessingUnit::process);
        }
        return true;
    }

    private static class ProcessingUnit {
        private final RoundEnvironment roundEnv;
        private final ProcessingEnvironment processingEnv;
        private final TypeElement typeElement;
        private final Types typeUtils;
        private final TypeMirror providerType;

        private ProcessingUnit(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv, TypeElement typeElement) {
            this.roundEnv = roundEnv;
            this.processingEnv = processingEnv;
            this.typeElement = typeElement;
            this.typeUtils = processingEnv.getTypeUtils();
            this.providerType = typeUtils.erasure(processingEnv.getElementUtils()
                    .getTypeElement("org.gradle.api.provider.Provider")
                    .asType());
        }

        private void process() {
            List<Property> properties = typeElement.getEnclosedElements()
                    .stream()
                    .filter(ExecutableElement.class::isInstance)
                    .map(ExecutableElement.class::cast)
                    .filter(this::isPropertyGetter)
                    .map(Property::new)
                    .collect(Collectors.toList());
            WasmProtocol protocolSpec = typeElement.getAnnotation(WasmProtocol.class);
            String simpleTaskName = taskNameFrom(protocolSpec);
            String functionName = protocolSpec.functionName();
            String library = libraryNameFrom(protocolSpec);
            String pkg = processingEnv.getElementUtils().getPackageOf(typeElement).toString();
            try (PrintWriter classWriter = createClassWriter(pkg, simpleTaskName)) {
                classWriter.println("package " + pkg + ";");
                TASK_IMPORTS.stream()
                        .map(fqn -> "import " + fqn + ";")
                        .forEachOrdered(classWriter::println);
                classWriter.println("import " + typeElement.getQualifiedName() + ";");
                classWriter.println();
                classWriter.println("public abstract class " + simpleTaskName + " extends AbstractSimpleWasmTask implements " + typeElement.getSimpleName() + " {");
                classWriter.println();
                classWriter.println("    public " + simpleTaskName + "() {");
                classWriter.println("        getFunctionName().set(\"" + functionName + "\");");
                classWriter.println("        fromClasspathLib(\"" + library + "\");");
                classWriter.println("    }");
                classWriter.println();
                classWriter.println("    @TaskAction");
                classWriter.println("    void execute() {");
                properties.stream()
                        .map(Property::getValueCode)
                        .map(code -> "        " + code)
                        .forEachOrdered(classWriter::println);
                classWriter.println("        Invoker invoker = Invoker.fromBinary(getWasmBinary().get()).build();");
                classWriter.println("        Object result = invoker.invokeSimple(\"" + functionName + "\", " + properties.stream().map(Property::getVarName).collect(Collectors.joining(", ")) + ");");
                classWriter.println("        System.out.println(\"Invocation result = \" + result);");
                classWriter.println("    }");
                classWriter.println("}");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private String libraryNameFrom(WasmProtocol protocolSpec) {
            String library = protocolSpec.classpathBinary();
            if (library.isEmpty()) {
                library = typeElement.getSimpleName().toString().toLowerCase();
            }
            return library;
        }

        private PrintWriter createClassWriter(String pkg, String taskName) throws IOException {
            return new PrintWriter(processingEnv.getFiler().createSourceFile(pkg + "." + taskName, typeElement).openWriter());
        }

        private String taskNameFrom(WasmProtocol protocolSpec) {
            String taskName = protocolSpec.taskName();
            if (taskName.isEmpty()) {
                Name simpleName = typeElement.getSimpleName();
                taskName = simpleName + "Task";
            }
            return taskName;
        }

        private boolean isPropertyGetter(ExecutableElement executableElement) {
            return executableElement.getKind() == ElementKind.METHOD
                    && executableElement.getSimpleName().toString().startsWith("get")
                    && executableElement.getParameters().isEmpty()
                    && isProviderType(executableElement.getReturnType());
        }

        private boolean isProviderType(TypeMirror returnType) {
            TypeMirror erasure = typeUtils.erasure(returnType);
            return typeUtils.isSubtype(erasure, providerType);
        }

        private TypeMirror providerTypeOf(TypeMirror type) {
            for (TypeMirror mirror : typeUtils.directSupertypes(type)) {
                if (typeUtils.isSameType(typeUtils.erasure(mirror), providerType)) {
                    return mirror;
                }
                TypeMirror providerMirror = providerTypeOf(mirror);
                if (providerMirror != null) {
                    return providerMirror;
                }
            }
            return null;
        }

        private static TypeMirror providedTypeOf(TypeMirror providerType) {
            if (providerType instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) providerType;
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (typeArguments.size() == 1) {
                    return typeArguments.get(0);
                }
            }
            return null;
        }

        private class Property {
            private final String methodName;
            private final String varName;
            private final String type;

            private Property(ExecutableElement getter) {
                TypeMirror providerType = providerTypeOf(getter.getReturnType());
                TypeMirror provides = providedTypeOf(providerType);
                this.methodName = getter.getSimpleName().toString();
                this.varName = propertyNameOf(getter.getSimpleName());
                this.type = provides.toString();
            }

            String getValueCode() {
                return type + " " + varName + " = " + methodName + "().get();";
            }

            public String getMethodName() {
                return methodName;
            }

            public String getVarName() {
                return varName;
            }

            public String getType() {
                return type;
            }
        }

        private static String propertyNameOf(Name simpleName) {
            String fullName = simpleName.toString();
            return Character.toLowerCase(fullName.charAt(3)) + fullName.substring(4);
        }
    }


}