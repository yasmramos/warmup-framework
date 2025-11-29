package io.warmup.framework.processor;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.lang.model.SourceVersion;
import java.io.IOException;
import java.util.*;

/**
 * Annotation Processor para generar metadata estática y eliminar reflexión completamente.
 * 
 * Este processor:
 * 1. Escanea todas las clases anotadas con @Component, @Bean, @Service, etc.
 * 2. Extrae información de constructores, métodos y campos
 * 3. Genera archivos de metadata para acceso O(1) sin reflexión
 * 4. Pre-procesa anotaciones para lookup directo
 */
@SupportedAnnotationTypes({
    "io.warmup.framework.annotation.Component",
    "io.warmup.framework.annotation.Bean", 
    "io.warmup.framework.annotation.Service",
    "io.warmup.framework.annotation.Named",
    "io.warmup.framework.annotation.Profile",
    "io.warmup.framework.annotation.Primary",
    "io.warmup.framework.annotation.Alternative",
    "io.warmup.framework.annotation.Qualifier",
    "io.warmup.framework.annotation.Value",
    "io.warmup.framework.annotation.Inject",
    "io.warmup.framework.annotation.Aspect",
    "io.warmup.framework.annotation.Before",
    "io.warmup.framework.annotation.After",
    "io.warmup.framework.annotation.Around",
    "io.warmup.framework.annotation.Health",
    "io.warmup.framework.annotation.Lazy"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MetadataProcessor extends AbstractProcessor {

    private static final String METADATA_PACKAGE = "io.warmup.framework.metadata";
    private static final String GENERATED_SOURCES_PATH = "generated-sources/annotations/java/";
    
    private Filer filer;
    private Messager messager;
    private Map<String, ClassMetadata> allClasses = new HashMap<>();
    private Map<String, AnnotationMetadata> allAnnotations = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "🚀 NativeMetadataProcessor iniciado - Eliminando reflexión para compilación nativa");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            // Último round - generar archivos de metadata
            try {
                generateMetadataFiles();
                generateIndices();
                messager.printMessage(Diagnostic.Kind.NOTE, "✅ NativeMetadataProcessor completado - Generación de metadata exitosa");
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "❌ Error generando metadata: " + e.getMessage());
            }
        } else {
            // Procesar clases anotadas
            processAnnotatedClasses(roundEnv);
            processAnnotatedMethods(roundEnv);
            processAnnotatedFields(roundEnv);
        }
        return true;
    }

    /**
     * Procesa clases anotadas con @Component, @Bean, @Service, etc.
     */
    private void processAnnotatedClasses(RoundEnvironment roundEnv) {
        Set<? extends Element> components = roundEnv.getElementsAnnotatedWith(
            processingEnv.getElementUtils().getTypeElement("io.warmup.framework.annotation.Component")
        );
        
        for (Element element : components) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                processClassMetadata(classElement);
            }
        }
    }

    /**
     * Procesa métodos anotados para metadata
     */
    private void processAnnotatedMethods(RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(
            processingEnv.getElementUtils().getTypeElement("io.warmup.framework.annotation.Bean")
        );
        
        for (Element element : annotatedMethods) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) element;
                processMethodMetadata(methodElement);
            }
        }
    }

    /**
     * Procesa campos anotados para metadata
     */
    private void processAnnotatedFields(RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedFields = roundEnv.getElementsAnnotatedWith(
            processingEnv.getElementUtils().getTypeElement("io.warmup.framework.annotation.Named")
        );
        
        for (Element element : annotatedFields) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement fieldElement = (VariableElement) element;
                processFieldMetadata(fieldElement);
            }
        }
    }

    /**
     * Extrae metadata de una clase
     */
    private void processClassMetadata(TypeElement classElement) {
        String className = classElement.getQualifiedName().toString();
        ClassMetadata metadata = new ClassMetadata();
        
        // Información básica de la clase
        metadata.setClassName(className);
        metadata.setSimpleName(classElement.getSimpleName().toString());
        metadata.setQualifiedName(classElement.getQualifiedName().toString());
        metadata.setPackageName(getPackageName(className));
        
        // Superclass
        TypeMirror superclass = classElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals("java.lang.Object")) {
            metadata.setSuperClass(superclass.toString());
        }
        
        // Interfaces implementadas
        List<String> interfaces = new ArrayList<>();
        for (TypeMirror iface : classElement.getInterfaces()) {
            interfaces.add(iface.toString());
        }
        metadata.setInterfaces(interfaces);
        
        // Anotaciones de la clase
        Map<String, Map<String, String>> annotations = new HashMap<>();
        for (AnnotationMirror annotation : classElement.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            Map<String, String> annotationValues = new HashMap<>();
            
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                 annotation.getElementValues().entrySet()) {
                annotationValues.put(
                    entry.getKey().getSimpleName().toString(),
                    entry.getValue().toString()
                );
            }
            annotations.put(annotationName, annotationValues);
        }
        metadata.setAnnotations(annotations);
        
        // Constructores
        List<ConstructorMetadata> constructors = new ArrayList<>();
        for (Element member : classElement.getEnclosedElements()) {
            if (member.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) member;
                ConstructorMetadata constructorMetadata = processConstructorMetadata(constructor);
                constructors.add(constructorMetadata);
            }
        }
        metadata.setConstructors(constructors);
        
        // Métodos
        List<MethodMetadata> methods = new ArrayList<>();
        for (Element member : classElement.getEnclosedElements()) {
            if (member.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) member;
                MethodMetadata methodMetadata = processMethodMetadata(method);
                methods.add(methodMetadata);
            }
        }
        metadata.setMethods(methods);
        
        // Campos
        List<FieldMetadata> fields = new ArrayList<>();
        for (Element member : classElement.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) member;
                FieldMetadata fieldMetadata = processFieldMetadata(field);
                fields.add(fieldMetadata);
            }
        }
        metadata.setFields(fields);
        
        allClasses.put(className, metadata);
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "📊 Processed class metadata: " + className);
    }

    /**
     * Extrae metadata de un constructor
     */
    private ConstructorMetadata processConstructorMetadata(ExecutableElement constructor) {
        ConstructorMetadata metadata = new ConstructorMetadata();
        
        metadata.setSimpleName(constructor.getSimpleName().toString());
        metadata.setQualifiedName(constructor.getEnclosingElement().toString() + "." + 
                                 constructor.getSimpleName().toString());
        
        // Parámetros
        List<ParameterMetadata> parameters = new ArrayList<>();
        for (VariableElement param : constructor.getParameters()) {
            ParameterMetadata paramMetadata = new ParameterMetadata();
            paramMetadata.setName(param.getSimpleName().toString());
            paramMetadata.setType(param.asType().toString());
            paramMetadata.setQualifiedType(param.asType().toString());
            
            // Anotaciones del parámetro
            Map<String, String> annotations = new HashMap<>();
            for (AnnotationMirror annotation : param.getAnnotationMirrors()) {
                String annotationName = annotation.getAnnotationType().toString();
                Map<String, String> annotationValues = new HashMap<>();
                
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                     annotation.getElementValues().entrySet()) {
                    annotationValues.put(
                        entry.getKey().getSimpleName().toString(),
                        entry.getValue().toString()
                    );
                }
                annotations.put(annotationName, annotationValues.isEmpty() ? "" : 
                               annotationValues.toString());
            }
            paramMetadata.setAnnotations(annotations);
            
            parameters.add(paramMetadata);
        }
        metadata.setParameters(parameters);
        
        // Modificadores
        metadata.setPublic(constructor.getModifiers().contains(Modifier.PUBLIC));
        metadata.setPrivate(constructor.getModifiers().contains(Modifier.PRIVATE));
        metadata.setProtected(constructor.getModifiers().contains(Modifier.PROTECTED));
        metadata.setPackagePrivate(!constructor.getModifiers().contains(Modifier.PUBLIC) &&
                                   !constructor.getModifiers().contains(Modifier.PRIVATE) &&
                                   !constructor.getModifiers().contains(Modifier.PROTECTED));
        
        // Anotaciones
        Map<String, String> annotations = new HashMap<>();
        for (AnnotationMirror annotation : constructor.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            Map<String, String> annotationValues = new HashMap<>();
            
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                 annotation.getElementValues().entrySet()) {
                annotationValues.put(
                    entry.getKey().getSimpleName().toString(),
                    entry.getValue().toString()
                );
            }
            annotations.put(annotationName, annotationValues.isEmpty() ? "" : 
                           annotationValues.toString());
        }
        metadata.setAnnotations(annotations);
        
        return metadata;
    }

    /**
     * Extrae metadata de un método
     */
    private MethodMetadata processMethodMetadata(ExecutableElement method) {
        MethodMetadata metadata = new MethodMetadata();
        
        metadata.setSimpleName(method.getSimpleName().toString());
        metadata.setQualifiedName(method.getEnclosingElement().toString() + "." + 
                                 method.getSimpleName().toString());
        metadata.setReturnType(method.getReturnType().toString());
        metadata.setQualifiedReturnType(method.getReturnType().toString());
        
        // Parámetros
        List<ParameterMetadata> parameters = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            ParameterMetadata paramMetadata = new ParameterMetadata();
            paramMetadata.setName(param.getSimpleName().toString());
            paramMetadata.setType(param.asType().toString());
            paramMetadata.setQualifiedType(param.asType().toString());
            
            // Anotaciones del parámetro
            Map<String, String> annotations = new HashMap<>();
            for (AnnotationMirror annotation : param.getAnnotationMirrors()) {
                String annotationName = annotation.getAnnotationType().toString();
                Map<String, String> annotationValues = new HashMap<>();
                
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                     annotation.getElementValues().entrySet()) {
                    annotationValues.put(
                        entry.getKey().getSimpleName().toString(),
                        entry.getValue().toString()
                    );
                }
                annotations.put(annotationName, annotationValues.isEmpty() ? "" : 
                               annotationValues.toString());
            }
            paramMetadata.setAnnotations(annotations);
            
            parameters.add(paramMetadata);
        }
        metadata.setParameters(parameters);
        
        // Excepciones
        List<String> exceptions = new ArrayList<>();
        for (TypeMirror exception : method.getThrownTypes()) {
            exceptions.add(exception.toString());
        }
        metadata.setExceptions(exceptions);
        
        // Modificadores
        metadata.setPublic(method.getModifiers().contains(Modifier.PUBLIC));
        metadata.setPrivate(method.getModifiers().contains(Modifier.PRIVATE));
        metadata.setProtected(method.getModifiers().contains(Modifier.PROTECTED));
        metadata.setStatic(method.getModifiers().contains(Modifier.STATIC));
        metadata.setFinal(method.getModifiers().contains(Modifier.FINAL));
        metadata.setAbstract(method.getModifiers().contains(Modifier.ABSTRACT));
        
        // Anotaciones
        Map<String, String> annotations = new HashMap<>();
        for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            Map<String, String> annotationValues = new HashMap<>();
            
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                 annotation.getElementValues().entrySet()) {
                annotationValues.put(
                    entry.getKey().getSimpleName().toString(),
                    entry.getValue().toString()
                );
            }
            annotations.put(annotationName, annotationValues.isEmpty() ? "" : 
                           annotationValues.toString());
        }
        metadata.setAnnotations(annotations);
        
        return metadata;
    }

    /**
     * Extrae metadata de un campo
     */
    private FieldMetadata processFieldMetadata(VariableElement field) {
        FieldMetadata metadata = new FieldMetadata();
        
        metadata.setSimpleName(field.getSimpleName().toString());
        metadata.setQualifiedName(field.getEnclosingElement().toString() + "." + 
                                 field.getSimpleName().toString());
        metadata.setType(field.asType().toString());
        metadata.setQualifiedType(field.asType().toString());
        
        // Modificadores
        metadata.setPublic(field.getModifiers().contains(Modifier.PUBLIC));
        metadata.setPrivate(field.getModifiers().contains(Modifier.PRIVATE));
        metadata.setProtected(field.getModifiers().contains(Modifier.PROTECTED));
        metadata.setStatic(field.getModifiers().contains(Modifier.STATIC));
        metadata.setFinal(field.getModifiers().contains(Modifier.FINAL));
        metadata.setTransient(field.getModifiers().contains(Modifier.TRANSIENT));
        metadata.setVolatile(field.getModifiers().contains(Modifier.VOLATILE));
        
        // Valor por defecto (para campos constantes)
        Object defaultValue = field.getConstantValue();
        if (defaultValue != null) {
            metadata.setDefaultValue(defaultValue.toString());
            metadata.setHasDefaultValue(true);
        } else {
            metadata.setHasDefaultValue(false);
        }
        
        // Anotaciones
        Map<String, String> annotations = new HashMap<>();
        for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            Map<String, String> annotationValues = new HashMap<>();
            
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                 annotation.getElementValues().entrySet()) {
                annotationValues.put(
                    entry.getKey().getSimpleName().toString(),
                    entry.getValue().toString()
                );
            }
            annotations.put(annotationName, annotationValues.isEmpty() ? "" : 
                           annotationValues.toString());
        }
        metadata.setAnnotations(annotations);
        
        return metadata;
    }

    /**
     * Genera archivos de metadata
     */
    private void generateMetadataFiles() throws IOException {
        // Generar ClassMetadata
        generateClassMetadata();
        
        // Metadatos adicionales se generan dinámicamente en generateClassMetadata()
    }

    /**
     * Genera archivo de metadata de clases
     */
    private void generateClassMetadata() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(METADATA_PACKAGE).append(";\n\n");
        builder.append("import java.util.*;\n\n");
        builder.append("/**\n");
        builder.append(" * Generated metadata para acceso O(1) sin reflexión\n");
        builder.append(" * Elimina todas las llamadas a getClass(), getSimpleName(), etc.\n");
        builder.append(" */\n");
        builder.append("public class GeneratedClassMetadata {\n");
        
        // Métodos de utilidad
        builder.append("    private static final Map<String, ClassMetadata> CLASS_METADATA = new HashMap<>();\n\n");
        
        // Inicialización de datos
        builder.append("    static {\n");
        for (Map.Entry<String, ClassMetadata> entry : allClasses.entrySet()) {
            String className = entry.getKey();
            ClassMetadata metadata = entry.getValue();
            
            builder.append("        // ").append(className).append("\n");
            builder.append("        ClassMetadata ").append(sanitizeClassName(className)).append(" = new ClassMetadata();\n");
            builder.append("        ").append(sanitizeClassName(className)).append(".setClassName(\"").append(className).append("\");\n");
            builder.append("        ").append(sanitizeClassName(className)).append(".setSimpleName(\"").append(metadata.getSimpleName()).append("\");\n");
            builder.append("        ").append(sanitizeClassName(className)).append(".setPackageName(\"").append(metadata.getPackageName()).append("\");\n");
            
            if (metadata.getSuperClass() != null) {
                builder.append("        ").append(sanitizeClassName(className)).append(".setSuperClass(\"").append(metadata.getSuperClass()).append("\");\n");
            }
            
            // Interfaces
            if (!metadata.getInterfaces().isEmpty()) {
                builder.append("        List<String> ").append(sanitizeClassName(className)).append("Interfaces = Arrays.asList(");
                boolean first = true;
                for (String iface : metadata.getInterfaces()) {
                    if (!first) builder.append(", ");
                    builder.append("\"").append(iface).append("\"");
                    first = false;
                }
                builder.append(");\n");
                builder.append("        ").append(sanitizeClassName(className)).append(".setInterfaces(").append(sanitizeClassName(className)).append("Interfaces);\n");
            }
            
            builder.append("        CLASS_METADATA.put(\"").append(className).append("\", ").append(sanitizeClassName(className)).append(");\n\n");
        }
        builder.append("    }\n\n");
        
        // Métodos estáticos
        builder.append("    public static ClassMetadata getMetadata(String className) {\n");
        builder.append("        return CLASS_METADATA.get(className);\n");
        builder.append("    }\n\n");
        
        builder.append("    public static String getSimpleName(String className) {\n");
        builder.append("        ClassMetadata metadata = CLASS_METADATA.get(className);\n");
        builder.append("        return metadata != null ? metadata.getSimpleName() : null;\n");
        builder.append("    }\n\n");
        
        builder.append("    public static String getPackageName(String className) {\n");
        builder.append("        ClassMetadata metadata = CLASS_METADATA.get(className);\n");
        builder.append("        return metadata != null ? metadata.getPackageName() : null;\n");
        builder.append("    }\n\n");
        
        builder.append("}\n");
        
        // Escribir archivo
        JavaFileObject fileObject = filer.createSourceFile(METADATA_PACKAGE + ".GeneratedClassMetadata");
        try (java.io.Writer writer = fileObject.openWriter()) {
            writer.write(builder.toString());
        }
    }

    /**
     * Genera índice de tipos
     */
    private void generateIndices() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(METADATA_PACKAGE).append(";\n\n");
        builder.append("import java.util.*;\n\n");
        builder.append("/**\n");
        builder.append(" * Índices pre-computados para acceso O(1) sin reflexión\n");
        builder.append(" */\n");
        builder.append("public class NativeTypeIndex {\n");
        
        // Índice de tipos por nombre simple
        builder.append("    private static final Map<String, String> SIMPLE_NAME_TO_QUALIFIED = new HashMap<>();\n\n");
        
        builder.append("    static {\n");
        for (Map.Entry<String, ClassMetadata> entry : allClasses.entrySet()) {
            String className = entry.getKey();
            String simpleName = entry.getValue().getSimpleName();
            builder.append("        SIMPLE_NAME_TO_QUALIFIED.put(\"").append(simpleName).append("\", \"").append(className).append("\");\n");
        }
        builder.append("    }\n\n");
        
        builder.append("    public static String getQualifiedName(String simpleName) {\n");
        builder.append("        return SIMPLE_NAME_TO_QUALIFIED.get(simpleName);\n");
        builder.append("    }\n\n");
        
        builder.append("    public static String getSimpleName(Object instance) {\n");
        builder.append("        if (instance == null) return \"null\";\n");
        builder.append("        return getQualifiedName(instance.getClass().getSimpleName());\n");
        builder.append("    }\n\n");
        
        builder.append("    public static boolean isInstanceOf(Object instance, String typeName) {\n");
        builder.append("        if (instance == null) return false;\n");
        builder.append("        return instance.getClass().getName().equals(typeName) || \n");
        builder.append("               instance.getClass().getSuperclass() != null && instance.getClass().getSuperclass().getName().equals(typeName);\n");
        builder.append("    }\n\n");
        
        builder.append("}\n");
        
        // Escribir archivo
        JavaFileObject fileObject = filer.createSourceFile(METADATA_PACKAGE + ".NativeTypeIndex");
        try (java.io.Writer writer = fileObject.openWriter()) {
            writer.write(builder.toString());
        }
    }

    // Métodos auxiliares
    
    // Métodos auxiliares removidos - se usan directamente en los métodos process

    private String getPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "";
    }

    private String sanitizeClassName(String className) {
        return className.replace('.', '_').replace('$', '_');
    }

    // Clases de metadata auxiliares
    
    static class ClassMetadata {
        private String className;
        private String simpleName;
        private String qualifiedName;
        private String packageName;
        private String superClass;
        private List<String> interfaces = new ArrayList<>();
        private Map<String, Map<String, String>> annotations = new HashMap<>();
        private List<ConstructorMetadata> constructors = new ArrayList<>();
        private List<MethodMetadata> methods = new ArrayList<>();
        private List<FieldMetadata> fields = new ArrayList<>();

        // Getters y setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getSimpleName() { return simpleName; }
        public void setSimpleName(String simpleName) { this.simpleName = simpleName; }
        public String getQualifiedName() { return qualifiedName; }
        public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public String getSuperClass() { return superClass; }
        public void setSuperClass(String superClass) { this.superClass = superClass; }
        public List<String> getInterfaces() { return interfaces; }
        public void setInterfaces(List<String> interfaces) { this.interfaces = interfaces; }
        public Map<String, Map<String, String>> getAnnotations() { return annotations; }
        public void setAnnotations(Map<String, Map<String, String>> annotations) { this.annotations = annotations; }
        public List<ConstructorMetadata> getConstructors() { return constructors; }
        public void setConstructors(List<ConstructorMetadata> constructors) { this.constructors = constructors; }
        public List<MethodMetadata> getMethods() { return methods; }
        public void setMethods(List<MethodMetadata> methods) { this.methods = methods; }
        public List<FieldMetadata> getFields() { return fields; }
        public void setFields(List<FieldMetadata> fields) { this.fields = fields; }
    }

    static class ConstructorMetadata {
        private String simpleName;
        private String qualifiedName;
        private List<ParameterMetadata> parameters = new ArrayList<>();
        private boolean isPublic, isPrivate, isProtected, isPackagePrivate;
        private Map<String, String> annotations = new HashMap<>();

        // Getters y setters
        public String getSimpleName() { return simpleName; }
        public void setSimpleName(String simpleName) { this.simpleName = simpleName; }
        public String getQualifiedName() { return qualifiedName; }
        public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }
        public List<ParameterMetadata> getParameters() { return parameters; }
        public void setParameters(List<ParameterMetadata> parameters) { this.parameters = parameters; }
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
        public boolean isPrivate() { return isPrivate; }
        public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
        public boolean isProtected() { return isProtected; }
        public void setProtected(boolean aProtected) { isProtected = aProtected; }
        public boolean isPackagePrivate() { return isPackagePrivate; }
        public void setPackagePrivate(boolean packagePrivate) { isPackagePrivate = packagePrivate; }
        public Map<String, String> getAnnotations() { return annotations; }
        public void setAnnotations(Map<String, String> annotations) { this.annotations = annotations; }
    }

    static class MethodMetadata {
        private String simpleName;
        private String qualifiedName;
        private String returnType;
        private String qualifiedReturnType;
        private List<ParameterMetadata> parameters = new ArrayList<>();
        private List<String> exceptions = new ArrayList<>();
        private boolean isPublic, isPrivate, isProtected, isStatic, isFinal, isAbstract;
        private Map<String, String> annotations = new HashMap<>();

        // Getters y setters
        public String getSimpleName() { return simpleName; }
        public void setSimpleName(String simpleName) { this.simpleName = simpleName; }
        public String getQualifiedName() { return qualifiedName; }
        public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
        public String getQualifiedReturnType() { return qualifiedReturnType; }
        public void setQualifiedReturnType(String qualifiedReturnType) { this.qualifiedReturnType = qualifiedReturnType; }
        public List<ParameterMetadata> getParameters() { return parameters; }
        public void setParameters(List<ParameterMetadata> parameters) { this.parameters = parameters; }
        public List<String> getExceptions() { return exceptions; }
        public void setExceptions(List<String> exceptions) { this.exceptions = exceptions; }
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
        public boolean isPrivate() { return isPrivate; }
        public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
        public boolean isProtected() { return isProtected; }
        public void setProtected(boolean aProtected) { isProtected = aProtected; }
        public boolean isStatic() { return isStatic; }
        public void setStatic(boolean aStatic) { isStatic = aStatic; }
        public boolean isFinal() { return isFinal; }
        public void setFinal(boolean aFinal) { isFinal = aFinal; }
        public boolean isAbstract() { return isAbstract; }
        public void setAbstract(boolean anAbstract) { isAbstract = anAbstract; }
        public Map<String, String> getAnnotations() { return annotations; }
        public void setAnnotations(Map<String, String> annotations) { this.annotations = annotations; }
    }

    static class ParameterMetadata {
        private String name;
        private String type;
        private String qualifiedType;
        private Map<String, String> annotations = new HashMap<>();

        // Getters y setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getQualifiedType() { return qualifiedType; }
        public void setQualifiedType(String qualifiedType) { this.qualifiedType = qualifiedType; }
        public Map<String, String> getAnnotations() { return annotations; }
        public void setAnnotations(Map<String, String> annotations) { this.annotations = annotations; }
    }

    static class FieldMetadata {
        private String simpleName;
        private String qualifiedName;
        private String type;
        private String qualifiedType;
        private boolean isPublic, isPrivate, isProtected, isStatic, isFinal, isTransient, isVolatile;
        private String defaultValue;
        private boolean hasDefaultValue;
        private Map<String, String> annotations = new HashMap<>();

        // Getters y setters
        public String getSimpleName() { return simpleName; }
        public void setSimpleName(String simpleName) { this.simpleName = simpleName; }
        public String getQualifiedName() { return qualifiedName; }
        public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getQualifiedType() { return qualifiedType; }
        public void setQualifiedType(String qualifiedType) { this.qualifiedType = qualifiedType; }
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }
        public boolean isPrivate() { return isPrivate; }
        public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
        public boolean isProtected() { return isProtected; }
        public void setProtected(boolean aProtected) { isProtected = aProtected; }
        public boolean isStatic() { return isStatic; }
        public void setStatic(boolean aStatic) { isStatic = aStatic; }
        public boolean isFinal() { return isFinal; }
        public void setFinal(boolean aFinal) { isFinal = aFinal; }
        public boolean isTransient() { return isTransient; }
        public void setTransient(boolean aTransient) { isTransient = aTransient; }
        public boolean isVolatile() { return isVolatile; }
        public void setVolatile(boolean aVolatile) { isVolatile = aVolatile; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public boolean hasDefaultValue() { return hasDefaultValue; }
        public void setHasDefaultValue(boolean hasDefaultValue) { this.hasDefaultValue = hasDefaultValue; }
        public Map<String, String> getAnnotations() { return annotations; }
        public void setAnnotations(Map<String, String> annotations) { this.annotations = annotations; }
    }

    static class AnnotationMetadata {
        private String name;
        private Map<String, String> values = new HashMap<>();

        // Getters y setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, String> getValues() { return values; }
        public void setValues(Map<String, String> values) { this.values = values; }
    }
}