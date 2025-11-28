package io.warmup.framework.proxy;

import io.warmup.framework.annotation.Async;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Visitor que transforma un método individual para hacerlo asíncrono
 */
final class AsyncMethodVisitor extends AdviceAdapter {
    
    private final String className;
    private final Class<?> targetClass;
    private final String methodName;
    private final Async asyncAnnotation;
    private final int timeout;
    private final String executorName;
    private final Async.ExceptionHandling exceptionHandling;
    
    protected AsyncMethodVisitor(MethodVisitor mv, int access, String name, 
                                String desc, String className, Class<?> targetClass, 
                                Async asyncAnnotation) {
        super(Opcodes.ASM9, mv, access, name, desc);
        this.className = className;
        this.targetClass = targetClass;
        this.methodName = name;
        this.asyncAnnotation = asyncAnnotation;
        this.timeout = (int) asyncAnnotation.timeout();
        this.executorName = asyncAnnotation.value();
        this.exceptionHandling = asyncAnnotation.exceptionHandling();
    }
    
    @Override
    protected void onMethodEnter() {
        // Crear un Callable para la ejecución asíncrona
        mv.visitTypeInsn(NEW, "java/util/concurrent/Callable");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0); // this
        
        // Crear instancia del Callable anónimo
        mv.visitInvokeDynamicInsn(
            "call", 
            "()Ljava/util/concurrent/Callable;", 
            new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false
            ),
            new Object[]{
                Type.getType("()Ljava/lang/Object;"),
                new Handle(
                    Opcodes.H_INVOKEVIRTUAL,
                    className,
                    "original$" + methodName,
                    getOriginalMethodDescriptor(),
                    false
                ),
                Type.getType("()Ljava/lang/Object;")
            }
        );
        
        // Llamar al AsyncExecutor con los parámetros de la anotación
        mv.visitMethodInsn(
            INVOKESTATIC,
            Type.getInternalName(io.warmup.framework.async.AsyncExecutor.class),
            "getInstance",
            "()Lio/warmup/framework/async/AsyncExecutor;",
            false
        );
        
        // Parámetro: nombre del executor
        mv.visitLdcInsn(executorName);
        
        // Parámetro: timeout
        mv.visitLdcInsn(timeout);
        
        // Parámetro: exception handling
        mv.visitFieldInsn(
            GETSTATIC,
            Type.getInternalName(Async.ExceptionHandling.class),
            exceptionHandling.name(),
            Type.getDescriptor(Async.ExceptionHandling.class)
        );
        
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            Type.getInternalName(io.warmup.framework.async.AsyncExecutor.class),
            "executeAsync",
            "(Ljava/lang/String;Ljava/util/concurrent/Callable;ILio/warmup/annotation/Async$ExceptionHandling;)Ljava/util/concurrent/CompletableFuture;",
            false
        );
        
        // Retornar el CompletableFuture inmediatamente
        mv.visitInsn(ARETURN);
    }
    
    @Override
    public void visitCode() {
        super.visitCode();
        // Este método será reemplazado por la lógica asíncrona
    }
    
    private String getOriginalMethodDescriptor() {
        try {
            java.lang.reflect.Method[] methods = targetClass.getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return Type.getMethodDescriptor(method);
                }
            }
        } catch (Exception e) {
            // Fallback a descriptor genérico
        }
        return "()Ljava/lang/Object;";
    }
}