package io.warmup.framework.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Visitor que transforma métodos anotados con @Async usando solo ASM
 */
public final class AsyncClassVisitor extends ClassVisitor {
    
    private final byte[] originalClassBytes;
    private String className;
    private ClassNode originalClassNode;
    private final Map<String, byte[]> generatedLambdas = new ConcurrentHashMap<>();
    private final AsyncClassLoader classLoader;
    
    public AsyncClassVisitor(ClassVisitor classVisitor, byte[] originalClassBytes, AsyncClassLoader classLoader) {
        super(Opcodes.ASM9, classVisitor);
        this.originalClassBytes = originalClassBytes;
        this.classLoader = classLoader;
        this.originalClassNode = parseOriginalClass();
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, 
                     String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                    String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        
        // Verificar si el método original tiene la anotación @Async usando ASM
        if (hasAsyncAnnotation(name, descriptor)) {
            return new AsyncMethodVisitor(mv, access, name, descriptor, className, originalClassBytes);
        }
        
        return mv;
    }
    
    /**
     * Parsea la clase original para obtener información de anotaciones
     */
    private ClassNode parseOriginalClass() {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(originalClassBytes);
        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return classNode;
    }
    
    /**
     * Verifica si un método tiene la anotación @Async usando solo ASM
     */
    private boolean hasAsyncAnnotation(String methodName, String descriptor) {
        for (Object methodObj : originalClassNode.methods) {
            MethodNode method = (MethodNode) methodObj;
            
            if (method.name.equals(methodName) && method.desc.equals(descriptor)) {
                // Verificar anotaciones en el método
                if (method.visibleAnnotations != null) {
                    for (AnnotationNode annotation : method.visibleAnnotations) {
                        if (isAsyncAnnotation(annotation)) {
                            return true;
                        }
                    }
                }
                
                // Verificar anotaciones invisibles (retention runtime)
                if (method.invisibleAnnotations != null) {
                    for (AnnotationNode annotation : method.invisibleAnnotations) {
                        if (isAsyncAnnotation(annotation)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Determina si una anotación es @Async
     */
    private boolean isAsyncAnnotation(AnnotationNode annotation) {
        // El descriptor de la anotación Async
        String asyncDescriptor = Type.getDescriptor(io.warmup.framework.annotation.Async.class);
        return annotation.desc.equals(asyncDescriptor);
    }
    
    /**
     * Carga una clase lambda generada dinámicamente
     */
    private Class<?> loadLambdaClass(String lambdaClassName, byte[] lambdaBytecode) {
        return classLoader.defineClass(lambdaClassName, lambdaBytecode);
    }
    
    /**
     * Visitor interno para métodos asincrónicos
     */
    private class AsyncMethodVisitor extends MethodVisitor {
        private final String methodName;
        private final String methodDescriptor;
        private final String className;
        private final byte[] originalClassBytes;
        
        public AsyncMethodVisitor(MethodVisitor methodVisitor, int access, 
                                String methodName, String descriptor, 
                                String className, byte[] originalClassBytes) {
            super(Opcodes.ASM9, methodVisitor);
            this.methodName = methodName;
            this.methodDescriptor = descriptor;
            this.className = className;
            this.originalClassBytes = originalClassBytes;
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Lógica de transformación asincrónica
            transformToAsync();
        }
        
        private void transformToAsync() {
            Type methodType = Type.getMethodType(methodDescriptor);
            Type returnType = methodType.getReturnType();
            Type[] argumentTypes = methodType.getArgumentTypes();
            
            // Generar y cargar la clase lambda primero
            String lambdaClassName = generateAndLoadLambdaClass(returnType, argumentTypes);
            
            // Crear CompletableFuture
            super.visitTypeInsn(Opcodes.NEW, "java/util/concurrent/CompletableFuture");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/concurrent/CompletableFuture", 
                                "<init>", "()V", false);
            super.visitVarInsn(Opcodes.ASTORE, 1); // Guardar future en variable local
            
            // Ejecutar en thread pool asincrónico
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/concurrent/Executors", 
                                "newCachedThreadPool", "()Ljava/util/concurrent/ExecutorService;", false);
            super.visitVarInsn(Opcodes.ASTORE, 2); // Guardar executor en variable local
            
            // Crear instancia del lambda
            super.visitTypeInsn(Opcodes.NEW, lambdaClassName.replace('.', '/'));
            super.visitInsn(Opcodes.DUP);
            
            // Pasar this y argumentos al constructor del lambda
            super.visitVarInsn(Opcodes.ALOAD, 0); // this
            
            // Cargar todos los parámetros del método original - FIXED: Corregir índices de variables locales
            int varIndex = 3; // 0 es this, 1 es future, 2 es executor, 3+ son argumentos del método
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argType = argumentTypes[i];
                super.visitVarInsn(argType.getOpcode(Opcodes.ILOAD), varIndex);
                varIndex += argType.getSize();
            }
            
            // Llamar constructor del lambda
            String constructorDesc = getLambdaConstructorDescriptor(argumentTypes);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, lambdaClassName.replace('.', '/'), 
                                "<init>", constructorDesc, false);
            
            // Submit la tarea al executor
            super.visitVarInsn(Opcodes.ALOAD, 2); // executor
            super.visitVarInsn(Opcodes.ALOAD, 1); // future
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, lambdaClassName.replace('.', '/'), 
                                "call", "()Ljava/lang/Object;", false);
            
            // Configurar el resultado en el CompletableFuture
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/CompletableFuture", 
                                "complete", "(Ljava/lang/Object;)Z", false);
            super.visitInsn(Opcodes.POP); // Descartar resultado boolean
            
            // Cerrar executor
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/concurrent/ExecutorService", 
                                "shutdown", "()V", true);
            
            // Retornar el CompletableFuture
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitInsn(Opcodes.ARETURN);
        }
        
        private String generateAndLoadLambdaClass(Type returnType, Type[] argumentTypes) {
            String lambdaClassName = className + "$$Lambda$" + methodName + "$" + 
                                   System.identityHashCode(this);
            
            System.out.println("[ASYNC VISITOR] Generating lambda for method: " + className + "." + methodName);
            System.out.println("[ASYNC VISITOR] Method descriptor: " + methodDescriptor);
            System.out.println("[ASYNC VISITOR] Return type: " + returnType);
            System.out.println("[ASYNC VISITOR] Argument types: " + argumentTypes.length);
            for (int i = 0; i < argumentTypes.length; i++) {
                System.out.println("[ASYNC VISITOR] Argument " + i + ": " + argumentTypes[i] + " (" + argumentTypes[i].getDescriptor() + ")");
            }
            
            // Generar bytecode de la clase lambda
            byte[] lambdaBytecode = LambdaClassGenerator.generateLambdaClass(
                lambdaClassName, 
                className,
                methodName, 
                methodDescriptor,
                returnType,
                argumentTypes
            );
            
            // Cargar la clase lambda
            loadLambdaClass(lambdaClassName, lambdaBytecode);
            
            return lambdaClassName;
        }
        
        private String getLambdaConstructorDescriptor(Type[] argumentTypes) {
            Type[] constructorArgs = new Type[argumentTypes.length + 1];
            constructorArgs[0] = Type.getObjectType(className);
            System.arraycopy(argumentTypes, 0, constructorArgs, 1, argumentTypes.length);
            return Type.getMethodDescriptor(Type.VOID_TYPE, constructorArgs);
        }
        
        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // ASM calculará esto automáticamente con COMPUTE_FRAMES
            super.visitMaxs(0, 0);
        }
    }
}