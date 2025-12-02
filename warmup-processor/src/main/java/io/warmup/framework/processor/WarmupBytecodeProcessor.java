package io.warmup.framework.processor;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.core.Dependency;
import io.warmup.framework.core.WarmupContainer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * WarmupBytecodeProcessor - Genera bytecode directamente en tiempo de compilación
 * 
 * Esta implementación sustituye la generación de código Java (.java) por la generación
 * de bytecode directamente (.class), eliminando la necesidad de classloaders personalizados
 * y mejorando el rendimiento al tener las clases generadas directamente en el JAR.
 */
@SupportedAnnotationTypes("io.warmup.framework.annotation.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WarmupBytecodeProcessor extends AbstractProcessor {

    private final Set<ComponentInfo> components = new HashSet<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private boolean processed = false;
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed) {
            return true;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "WarmupBytecodeProcessor: Generando bytecode directamente...");

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
            generateBytecodeDependencyResolvers();
            processed = true;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generación de bytecode completado: " + components.size() + " componentes");
        }

        return true;
    }

    private void generateBytecodeDependencyResolvers() {
        String resolversPackage = "io.warmup.framework.generated.resolvers";

        for (ComponentInfo info : components) {
            generateIndividualResolverBytecode(resolversPackage, info);
        }

        // Generar el Registry en bytecode también
        generateResolverRegistryBytecode(resolversPackage);
    }

    private void generateIndividualResolverBytecode(String packageName, ComponentInfo info) {
        String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);
        String className = simpleName + "Resolver";
        String fullyQualifiedName = packageName + "." + className;

        try {
            ClassWriter cw = new ClassWriter(0);
            
            // Definir la clase
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                    fullyQualifiedName.replace('.', '/'), 
                    null,
                    "io/warmup/framework/core/Dependency",
                    null);

            // Constructor público
            MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0); // this
            constructor.visitLdcInsn(Class.forName(info.className));
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    "io/warmup/framework/core/Dependency", "<init>", 
                    "(Ljava/lang/Class;Z)V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(2, 1);
            constructor.visitEnd();

            // Método getInstance
            generateGetInstanceMethod(cw, info);

            cw.visitEnd();
            
            // Escribir el bytecode
            byte[] bytecode = cw.toByteArray();
            
            try (OutputStream outputStream = filer.createClassFile(fullyQualifiedName).openOutputStream()) {
                outputStream.write(bytecode);
                
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Generado bytecode: " + fullyQualifiedName + ".class");
            }

        } catch (ClassNotFoundException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Clase no encontrada: " + info.className);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando bytecode para " + info.className + ": " + e.getMessage());
        }
    }

    private void generateGetInstanceMethod(ClassWriter cw, ComponentInfo info) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getInstance", 
                "(Lio/warmup/framework/core/WarmupContainer;Ljava/util/Set;)Ljava/lang/Object;", 
                null, null);
        mv.visitCode();

        // Obtener información del constructor usando ASM
        ConstructorData data = extractConstructorDataFromASM(info.className);
        
        if (data != null && data.paramTypes.length > 0) {
            // Generar: return new ComponentClass(param1, param2, ...)
            mv.visitTypeInsn(Opcodes.NEW, info.className.replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            
            // Cargar argumentos en la pila
            for (int i = 0; i < data.paramTypes.length; i++) {
                String type = data.paramTypes[i];
                String named = data.paramNames[i];

                // Cargar el parámetro según su tipo
                mv.visitVarInsn(Opcodes.ALOAD, 1); // container
                
                if (named != null) {
                    // container.getNamed(type.class, "named")
                    mv.visitLdcInsn(getTypeClass(type));
                    mv.visitLdcInsn(named);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "io/warmup/framework/core/WarmupContainer", "getNamed", 
                            "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;", false);
                } else {
                    // container.get(type.class)
                    mv.visitLdcInsn(getTypeClass(type));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "io/warmup/framework/core/WarmupContainer", "get", 
                            "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                }
                
                // Conversión de tipos según sea necesario
                generateTypeCast(mv, type);
            }
            
            // Llamar al constructor
            String constructorDescriptor = generateConstructorDescriptor(data.paramTypes);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    info.className.replace('.', '/'), "<init>", constructorDescriptor, false);
            
        } else {
            // Fallback: llamar al método padre
            mv.visitVarInsn(Opcodes.ALOAD, 0); // this
            mv.visitVarInsn(Opcodes.ALOAD, 1); // container
            mv.visitVarInsn(Opcodes.ALOAD, 2); // dependencyChain
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    "io/warmup/framework/core/Dependency", "getInstance", 
                    "(Lio/warmup/framework/core/WarmupContainer;Ljava/util/Set;)Ljava/lang/Object;", false);
        }

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(16, 16);
        mv.visitEnd();
    }

    private String generateConstructorDescriptor(String[] paramTypes) {
        StringBuilder descriptor = new StringBuilder("(");
        for (String type : paramTypes) {
            descriptor.append("L").append(type.replace('.', '/')).append(";");
        }
        descriptor.append(")V");
        return descriptor.toString();
    }

    private void generateTypeCast(MethodVisitor mv, String type) {
        // Implementar conversiones de tipos básicas
        switch (type) {
            case "int":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case "long":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case "boolean":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case "double":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case "float":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            // Para tipos primitivos cortos y byte, etc.
            default:
                // Para tipos de objeto, solo el cast es suficiente
                mv.visitTypeInsn(Opcodes.CHECKCAST, type.replace('.', '/'));
                break;
        }
    }

    private void generateResolverRegistryBytecode(String packageName) {
        String className = "ResolverRegistry";
        String fullyQualifiedName = packageName + "." + className;

        try (OutputStream outputStream = filer.createClassFile(fullyQualifiedName).openOutputStream()) {
            ClassWriter cw = new ClassWriter(0);
            
            // Definir la clase pública
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                    fullyQualifiedName.replace('.', '/'), 
                    null,
                    "java/lang/Object",
                    null);

            // Campo estático para las resoluciones
            FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
                    "RESOLUTIONS", "Ljava/util/Map;", 
                    "Ljava/util/Map<Ljava/lang/Class;>;", null);
            fv.visitEnd();

            // Constructor privado (no instanciable)
            MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0); // this
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();

            // Método estático de inicialización
            MethodVisitor initMethod = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
                    "initialize", "()V", null, null);
            initMethod.visitCode();

            // Crear el mapa de resoluciones
            initMethod.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
            initMethod.visitInsn(Opcodes.DUP);
            initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                    "java/util/HashMap", "<init>", "()V", false);
            
            int index = 0;
            for (ComponentInfo info : components) {
                String resolverClass = packageName + "." + info.className.substring(info.className.lastIndexOf('.') + 1) + "Resolver";
                
                // Poner en el mapa: Map.put(Interface.class, new Resolver())
                initMethod.visitInsn(Opcodes.DUP); // Duplicar la referencia al mapa
                try {
                    initMethod.visitLdcInsn(Class.forName(info.className));
                } catch (ClassNotFoundException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Clase no encontrada: " + info.className);
                    continue;
                }
                
                // Crear instancia del resolver
                initMethod.visitTypeInsn(Opcodes.NEW, resolverClass.replace('.', '/'));
                initMethod.visitInsn(Opcodes.DUP);
                initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                        resolverClass.replace('.', '/'), "<init>", "()V", false);
                
                initMethod.visitMethodInsn(Opcodes.INVOKEINTERFACE, 
                        "java/util/Map", "put", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                initMethod.visitInsn(Opcodes.POP); // Pop del resultado de put
                
                index++;
            }

            // Guardar en el campo estático
            initMethod.visitFieldInsn(Opcodes.PUTSTATIC, 
                    fullyQualifiedName.replace('.', '/'), "RESOLUTIONS", 
                    "Ljava/util/Map;");
            initMethod.visitInsn(Opcodes.RETURN);
            initMethod.visitMaxs(32, 16);
            initMethod.visitEnd();

            cw.visitEnd();
            
            byte[] bytecode = cw.toByteArray();
            outputStream.write(bytecode);
            
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generado bytecode: " + fullyQualifiedName + ".class");

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error generando ResolverRegistry bytecode: " + e.getMessage());
        }
    }

    // Métodos existentes adaptados para el nuevo procesador
    private ComponentInfo extractComponentInfo(TypeElement element) {
        // Implementación igual que el procesador original
        ComponentInfo info = new ComponentInfo();
        info.className = element.getQualifiedName().toString();
        info.singleton = element.getAnnotation(Component.class).singleton();
        
        return info;
    }

    private void performCompileTimeValidations() {
        // Implementar validaciones en tiempo de compilación
        for (ComponentInfo info : components) {
            TypeElement element = elementUtils.getTypeElement(info.className);
            if (element == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Componente no encontrado: " + info.className);
            }
        }
    }

    private void analyzeDependencies(TypeElement component) {
        // Implementar análisis de dependencias como en el procesador original
        String componentName = component.getQualifiedName().toString();
        dependencyGraph.put(componentName, new HashSet<>());
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
            }, 0);

            if (types.isEmpty()) {
                return null;
            }

            ConstructorData data = new ConstructorData();
            data.paramTypes = new String[types.size()];
            data.paramNames = new String[names.size()];

            for (int i = 0; i < types.size(); i++) {
                data.paramTypes[i] = getClassNameFromType(types.get(i));
                data.paramNames[i] = names.get(i);
            }

            return data;
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "No se pudo analizar constructor de " + className + ": " + e.getMessage());
            return null;
        }
    }

    private String getClassNameFromType(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN: return "boolean";
            case Type.BYTE: return "byte";
            case Type.CHAR: return "char";
            case Type.DOUBLE: return "double";
            case Type.FLOAT: return "float";
            case Type.INT: return "int";
            case Type.LONG: return "long";
            case Type.SHORT: return "short";
            case Type.VOID: return "void";
            case Type.ARRAY:
                return getClassNameFromType(type.getElementType()) + "[]";
            case Type.OBJECT:
                return type.getClassName();
            default:
                return "java.lang.Object";
        }
    }

    private Class<?> getTypeClass(String typeName) {
        try {
            switch (typeName) {
                case "int": return int.class;
                case "long": return long.class;
                case "boolean": return boolean.class;
                case "double": return double.class;
                case "float": return float.class;
                case "byte": return byte.class;
                case "char": return char.class;
                case "short": return short.class;
                default: return Class.forName(typeName);
            }
        } catch (ClassNotFoundException e) {
            return Object.class;
        }
    }

    static class ComponentInfo {
        String className;
        boolean singleton;
    }
}