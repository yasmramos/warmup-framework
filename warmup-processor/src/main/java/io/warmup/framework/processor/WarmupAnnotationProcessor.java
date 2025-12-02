package io.warmup.framework.processor;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Named;
import io.warmup.framework.asm.AsmCoreUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@SupportedAnnotationTypes("io.warmup.framework.annotation.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WarmupAnnotationProcessor extends AbstractProcessor {

    public WarmupAnnotationProcessor() {
        // Public no-arg constructor required for annotation processors
    }

    private final Set<ComponentInfo> components = new HashSet<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private boolean processed = false;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed) {
            return true;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "WarmupAnnotationProcessor: Buscando y validando componentes...");

        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                ComponentInfo info = extractComponentInfo(typeElement);
                if (info != null && info.className != null) {
                    components.add(info);
                    analyzeDependencies(typeElement);
                }
            }
        }

        if (!components.isEmpty()) {
            performCompileTimeValidations();
        }

        if (roundEnv.processingOver() && !components.isEmpty()) {
            generateConfigClass();
            generateDependencyResolvers();
            processed = true;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generación de código pre-compilado completada");
        }

        return true;
    }

    private void generateDependencyResolvers() {
        String resolversPackage = "io.warmup.framework.generated.resolvers";
        generateASMAnalyzerClass();

        for (ComponentInfo info : components) {
            generateIndividualResolver(resolversPackage, info);
        }

        generateResolverRegistry(resolversPackage);
    }

    private void generateIndividualResolver(String packageName, ComponentInfo info) {
        String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);
        String fullClassName = info.className.replace('.', '_');
        String className = fullClassName + "Resolver";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("import io.warmup.framework.core.IContainer;");
                out.println("import io.warmup.framework.core.Dependency;");
                out.println("import java.util.Set;");
                out.println();
                out.println("public class " + className + " extends Dependency {");
                out.println("    public " + className + "() {");
                out.println("        super(" + info.className + ".class, " + info.singleton + ");");
                out.println("    }");
                out.println();
                out.println("    @Override");
                out.println("    public Object getInstance(IContainer container, Set<Class<?>> dependencyChain) {");

                ConstructorData data = extractConstructorDataFromASM(info.className);

                if (data != null && data.paramTypes.length > 0) {
                    out.println("        return new " + info.className + "(");
                    for (int i = 0; i < data.paramTypes.length; i++) {
                        String type = data.paramTypes[i];
                        String named = data.paramNames[i];

                        if (named != null) {
                            out.println("            (" + type + ") container.getNamed(" + type + ".class, \"" + named + "\"),");
                        } else {
                            out.println("            (" + type + ") container.get(" + type + ".class),");
                        }
                    }
                    out.println("        );");
                } else {
                    out.println("        return super.getInstance(container, dependencyChain);");
                }

                out.println("    }");
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando resolvedor para " + info.className + ": " + e.getMessage());
        }
    }

    private void generateASMAnalyzerClass() {
        String packageName = "io.warmup.framework.generated.asm";
        String className = "ConstructorAnalyzer";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("import org.objectweb.asm.*;");
                out.println("import java.util.*;");
                out.println();
                out.println("public class " + className + " extends ClassVisitor {");
                out.println("    private final List<ConstructorInfo> constructors = new ArrayList<>();");
                out.println();
                out.println("    public " + className + "() { super(Opcodes.ASM9); }");
                out.println();
                out.println("    @Override");
                out.println("    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {");
                out.println("        if (\"<init>\".equals(name)) {");
                out.println("            Type[] types = Type.getArgumentTypes(descriptor);");
                out.println("            ConstructorInfo info = new ConstructorInfo(types);");
                out.println("            constructors.add(info);");
                out.println("            return new MethodVisitor(api) {");
                out.println("                @Override");
                out.println("                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {");
                out.println("                    if (\"Lio/warmup/framework/annotation/Named;\".equals(desc)) {");
                out.println("                        return new AnnotationVisitor(api) {");
                out.println("                            @Override");
                out.println("                            public void visit(String name, Object value) {");
                out.println("                                info.setParameterName(parameter, (String) value);");
                out.println("                            }");
                out.println("                        };");
                out.println("                    }");
                out.println("                    return null;");
                out.println("                }");
                out.println("            };");
                out.println("        }");
                out.println("        return null;");
                out.println("    }");
                out.println();
                out.println("    public List<ConstructorInfo> getConstructors() { return constructors; }");
                out.println();
                out.println("    public static class ConstructorInfo {");
                out.println("        private final Type[] paramTypes;");
                out.println("        private final String[] paramNames;");
                out.println("        public ConstructorInfo(Type[] paramTypes) {");
                out.println("            this.paramTypes = paramTypes;");
                out.println("            this.paramNames = new String[paramTypes.length];");
                out.println("        }");
                out.println("        public void setParameterName(int index, String name) { paramNames[index] = name; }");
                out.println("        public Type[] getParamTypes() { return paramTypes; }");
                out.println("        public String[] getParamNames() { return paramNames; }");
                out.println("    }");
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando ConstructorAnalyzer: " + e.getMessage());
        }
    }

    private void generateResolverRegistry(String resolversPackage) {
        String packageName = "io.warmup.framework.generated";
        String className = "PrecompiledResolverRegistry";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("import io.warmup.framework.core.DependencyRegistry;");
                out.println("import " + resolversPackage + ".*;");
                out.println();
                out.println("public class " + className + " {");
                out.println("    public static void registerAllResolvers(DependencyRegistry registry) {");

                for (ComponentInfo info : components) {
                    String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);
                    String fullClassName = info.className.replace('.', '_');
                    out.println("        registry.getDependencies().put(" + info.className + ".class, new " + fullClassName + "Resolver());");
                }

                out.println("    }");
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando resolver registry: " + e.getMessage());
        }
    }

    private ConstructorData extractConstructorDataFromASM(String className) {
        try {
            String resource = className.replace('.', '/') + ".class";
            InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
            if (is == null) {
                return null;
            }

            ClassReader reader = new ClassReader(is);
            List<Type> types = new ArrayList<>();
            List<String> names = new ArrayList<>();

            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if ("<init>".equals(name)) {
                        Type[] argTypes = Type.getArgumentTypes(descriptor);
                        types.addAll(Arrays.asList(argTypes));
                        names.addAll(Collections.nCopies(argTypes.length, null));

                        return new MethodVisitor(api) {
                            @Override
                            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                                if ("Lio/warmup/framework/annotation/Named;".equals(desc)) {
                                    return new AnnotationVisitor(api) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            names.set(parameter, (String) value);
                                        }
                                    };
                                }
                                return null;
                            }
                        };
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            ConstructorData data = new ConstructorData();
            data.paramTypes = types.stream().map(Type::getClassName).toArray(String[]::new);
            data.paramNames = names.toArray(new String[0]);
            return data;

        } catch (IOException e) {
            return null;
        }
    }

    private void generateConfigClass() {
        String packageName = "io.warmup.framework.generated";
        String className = "PrecompiledComponentConfig";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("import io.warmup.framework.core.IContainer;");
                out.println();
                out.println("public class " + className + " {");
                out.println("    public static void registerAllComponents(IContainer container) {");

                for (ComponentInfo info : components) {
                    if (info.named != null) {
                        out.println("        container.registerNamed(" + info.className + ".class, \"" + info.named + "\", " + info.singleton + ");");
                    } else {
                        out.println("        container.register(" + info.className + ".class, " + info.singleton + ");");
                    }
                }

                out.println("    }");
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando configuración: " + e.getMessage());
        }
    }

    private ComponentInfo extractComponentInfo(TypeElement element) {
        Component component = AsmCoreUtils.getAnnotationProgressive(element, Component.class);
        Named named = AsmCoreUtils.getAnnotationProgressive(element, Named.class);
        String className = element.getQualifiedName().toString();
        boolean singleton = component != null && component.singleton();
        String namedValue = (named != null && !named.value().trim().isEmpty()) ? named.value().trim() : null;
        return new ComponentInfo(className, singleton, namedValue, false);
    }

    private ExecutableElement findTargetConstructor(TypeElement element) {
        List<ExecutableElement> constructors = new ArrayList<>();

        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                constructors.add((ExecutableElement) enclosed);
            }
        }

        // Buscar constructor con @Inject  
        for (ExecutableElement constructor : constructors) {
            if (AsmCoreUtils.getAnnotationProgressive(constructor, io.warmup.framework.annotation.Inject.class) != null) {
                return constructor;
            }
        }

        // Si no hay @Inject, buscar constructor sin parámetros  
        for (ExecutableElement constructor : constructors) {
            if (constructor.getParameters().isEmpty()) {
                return constructor;
            }
        }

        // Si no hay constructor sin parámetros, usar el primero  
        return constructors.isEmpty() ? null : constructors.get(0);
    }

    private String generateParameterResolution(VariableElement param) {
        TypeMirror paramType = param.asType();
        String paramTypeStr = paramType.toString();

        // Verificar si tiene @Named  
        io.warmup.framework.annotation.Named namedAnnotation
                = AsmCoreUtils.getAnnotationProgressive(param, io.warmup.framework.annotation.Named.class);
        if (namedAnnotation != null) {
            String namedValue = namedAnnotation.value();
            return "(" + paramTypeStr + ") container.getNamed("
                    + getClassLiteral(paramTypeStr) + ", \"" + namedValue + "\")";
        }

        // Para tipos genéricos  
        if (paramTypeStr.contains("<") && paramTypeStr.contains(">")) {
            return resolveGenericType(param, paramTypeStr);
        }

        // Para tipos básicos de Java  
        if (isJavaType(paramTypeStr)) {
            String paramName = param.getSimpleName().toString();
            return "(" + paramTypeStr + ") container.getNamed("
                    + getClassLiteral(paramTypeStr) + ", \"" + paramName + "\")";
        }

        // Dependencia normal  
        return "(" + paramTypeStr + ") container.get(" + getClassLiteral(paramTypeStr) + ")";
    }

    private String resolveGenericType(VariableElement param, String paramTypeStr) {
        // TODO: Implementar resolución completa de tipos genéricos
        // Por ahora, extraemos el tipo base y generamos una llamada genérica
        if (paramTypeStr.startsWith("List<")) {
            String elementType = paramTypeStr.substring(5, paramTypeStr.length() - 1);
            return "(java.util.List<" + elementType + ">) container.get(java.util.List.class)";
        } else if (paramTypeStr.startsWith("Map<")) {
            String[] parts = paramTypeStr.substring(4, paramTypeStr.length() - 1).split(",", 2);
            String keyType = parts[0].trim();
            String valueType = parts[1].trim();
            return "(java.util.Map<" + keyType + ", " + valueType + ">) container.get(java.util.Map.class)";
        } else {
            // Fallback: extraer tipo base y usar get() genérico
            return "(java.util.List) container.get(java.util.List.class)";
        }
    }

    private String getClassLiteral(String typeName) {
        // Manejar tipos primitivos  
        if (typeName.equals("int")) {
            return "int.class";
        }
        if (typeName.equals("long")) {
            return "long.class";
        }
        if (typeName.equals("boolean")) {
            return "boolean.class";
        }
        if (typeName.equals("double")) {
            return "double.class";
        }
        if (typeName.equals("float")) {
            return "float.class";
        }
        if (typeName.equals("byte")) {
            return "byte.class";
        }
        if (typeName.equals("short")) {
            return "short.class";
        }
        if (typeName.equals("char")) {
            return "char.class";
        }

        // Para tipos con genéricos, extraer el tipo base  
        if (typeName.contains("<")) {
            typeName = typeName.substring(0, typeName.indexOf('<'));
        }

        return typeName + ".class";
    }

    private void generateConstructorCode(PrintWriter out, TypeElement element, String className) {
        try {
            ExecutableElement targetConstructor = findTargetConstructor(element);

            if (targetConstructor == null) {
                out.println("        return super.getInstance(container, dependencyChain);");
                return;
            }

            List<? extends VariableElement> parameters = targetConstructor.getParameters();

            if (parameters.isEmpty()) {
                out.println("        instance = new " + className + "();");
            } else {
                out.println("        instance = new " + className + "(");
                for (int i = 0; i < parameters.size(); i++) {
                    VariableElement param = parameters.get(i);
                    String paramResolution = generateParameterResolution(param);
                    out.print("            " + paramResolution);
                    if (i < parameters.size() - 1) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println("        );");
            }

        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando constructor para " + className + ": " + e.getMessage());
            out.println("        return super.getInstance(container, dependencyChain);");
        }
    }

    private void analyzeDependencies(TypeElement component) {
        String componentName = component.getQualifiedName().toString();
        dependencyGraph.put(componentName, new HashSet<>());

        // Analizar constructores con @Inject  
        for (Element enclosed : component.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR
                    && AsmCoreUtils.getAnnotationProgressive(enclosed, io.warmup.framework.annotation.Inject.class) != null) {
                analyzeConstructorDependencies((ExecutableElement) enclosed, componentName);
            }
        }

        // Analizar campos con @Inject  
        for (Element enclosed : component.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD
                    && AsmCoreUtils.getAnnotationProgressive(enclosed, io.warmup.framework.annotation.Inject.class) != null) {
                analyzeFieldDependencies((VariableElement) enclosed, componentName);
            }
        }

        // Analizar métodos con @Inject  
        for (Element enclosed : component.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD
                    && AsmCoreUtils.getAnnotationProgressive(enclosed, io.warmup.framework.annotation.Inject.class) != null) {
                analyzeMethodDependencies((ExecutableElement) enclosed, componentName);
            }
        }
    }

    private void analyzeConstructorDependencies(ExecutableElement constructor, String componentName) {
        for (VariableElement parameter : constructor.getParameters()) {
            TypeMirror paramType = parameter.asType();
            String dependencyName = extractDependencyName(parameter, paramType);

            if (dependencyName != null && !isJavaType(dependencyName)) {
                dependencyGraph.get(componentName).add(dependencyName);
            }
        }
    }

    private void analyzeFieldDependencies(VariableElement field, String componentName) {
        TypeMirror fieldType = field.asType();
        String dependencyName = extractDependencyName(field, fieldType);

        if (dependencyName != null && !isJavaType(dependencyName)) {
            dependencyGraph.get(componentName).add(dependencyName);
        }
    }

    private void analyzeMethodDependencies(ExecutableElement method, String componentName) {
        for (VariableElement parameter : method.getParameters()) {
            TypeMirror paramType = parameter.asType();
            String dependencyName = extractDependencyName(parameter, paramType);

            if (dependencyName != null && !isJavaType(dependencyName)) {
                dependencyGraph.get(componentName).add(dependencyName);
            }
        }
    }

    private void performCompileTimeValidations() {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "Realizando validaciones en compile-time...");

        detectDependencyCycles();
        validateMissingDependencies();
        validateNamedAnnotations();
        validateConstructors();
    }

    private void detectDependencyCycles() {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();

        for (String component : dependencyGraph.keySet()) {
            if (hasCycle(component, visited, recursionStack, new ArrayList<>())) {
                break;
            }
        }
    }

    private boolean hasCycle(String component, Map<String, Boolean> visited,
            Map<String, Boolean> recursionStack, List<String> path) {
        if (recursionStack.getOrDefault(component, false)) {
            path.add(component);
            reportCycle(path);
            return true;
        }

        if (visited.getOrDefault(component, false)) {
            return false;
        }

        visited.put(component, true);
        recursionStack.put(component, true);
        path.add(component);

        Set<String> dependencies = dependencyGraph.get(component);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (hasCycle(dependency, visited, recursionStack, new ArrayList<>(path))) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        recursionStack.put(component, false);
        return false;
    }

    private void reportCycle(List<String> cycle) {
        StringBuilder cycleMessage = new StringBuilder();
        cycleMessage.append("CICLO DE DEPENDENCIAS DETECTADO: ");

        for (int i = 0; i < cycle.size(); i++) {
            if (i > 0) {
                cycleMessage.append(" → ");
            }
            cycleMessage.append(cycle.get(i));
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, cycleMessage.toString());
    }

    private void validateMissingDependencies() {
        Set<String> allComponentClasses = new HashSet<>();
        for (ComponentInfo info : components) {
            allComponentClasses.add(info.className);
        }

        for (Map.Entry<String, Set<String>> entry : dependencyGraph.entrySet()) {
            String component = entry.getKey();
            Set<String> dependencies = entry.getValue();

            for (String dependency : dependencies) {
                if (!isDependencyRegistered(dependency) && !isJavaType(dependency)) {
                    if (classExists(dependency)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                "CLASE EXISTE PERO NO REGISTRADA: " + dependency
                                + " existe en el classpath pero no está anotada con @Component");
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "CLASE NO ENCONTRADA: " + component + " requiere " + dependency
                                + " pero la clase no existe");
                    }
                }
            }
        }
    }

    private boolean classExists(String className) {
        try {
            elementUtils.getTypeElement(className);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateNamedAnnotations() {
        for (ComponentInfo info : components) {
            if (info.named != null && info.named.trim().isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@Named SIN VALOR: " + info.className + " tiene @Named pero sin valor especificado");
            }
        }
    }

    private void validateConstructors() {
        for (ComponentInfo info : components) {
            try {
                Class<?> clazz = Class.forName(info.className);
                java.lang.reflect.Constructor<?>[] constructors = clazz.getDeclaredConstructors();

                if (constructors.length > 1) {
                    boolean hasInject = false;
                    for (java.lang.reflect.Constructor<?> constructor : constructors) {
                        if (constructor.isAnnotationPresent(io.warmup.framework.annotation.Inject.class)) {
                            hasInject = true;
                            break;
                        }
                    }

                    if (!hasInject) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                "MÚLTIPLES CONSTRUCTORES: " + info.className
                                + " tiene múltiples constructores pero ninguno con @Inject");
                    }
                }
            } catch (ClassNotFoundException e) {
                // Ignorar - no disponible en compile-time  
            }
        }
    }

    private boolean isDependencyRegistered(String dependency) {
        return components.stream()
                .anyMatch(info -> info.className.equals(dependency));
    }

    private boolean isJavaType(String className) {
        return className.startsWith("java.")
                || className.startsWith("javax.")
                || className.equals("int") || className.equals("boolean")
                || className.equals("long") || className.equals("double")
                || className.equals("float") || className.equals("byte")
                || className.equals("short") || className.equals("char")
                || className.equals("String")
                || className.equals("io.warmup.framework.core.IContainer");
    }

    private String extractDependencyName(Element element, TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
            return typeElement.getQualifiedName().toString();
        }

        if (type.getKind() == TypeKind.DECLARED && type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();

            if (typeElement.getQualifiedName().toString().equals("java.util.List")
                    || typeElement.getQualifiedName().toString().equals("java.util.Map")) {

                List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                if (!typeArgs.isEmpty() && typeArgs.get(0) instanceof DeclaredType) {
                    TypeElement genericElement = (TypeElement) ((DeclaredType) typeArgs.get(0)).asElement();
                    return genericElement.getQualifiedName().toString();
                }
            }

            return typeElement.getQualifiedName().toString();
        }

        return null;
    }
}
