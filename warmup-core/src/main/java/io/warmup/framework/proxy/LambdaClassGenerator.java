package io.warmup.framework.proxy;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generador de clases lambda para métodos asincrónicos
 */
public final class LambdaClassGenerator implements Opcodes {

    public static byte[] generateLambdaClass(String lambdaClassName,
            String ownerClassName,
            String methodName,
            String methodDescriptor,
            Type returnType,
            Type[] argumentTypes) {

        System.out.println("[DEBUG] Generating lambda class: " + lambdaClassName);
        System.out.println("[DEBUG] Owner class: " + ownerClassName);
        System.out.println("[DEBUG] Method: " + methodName);
        System.out.println("[DEBUG] Method descriptor: " + methodDescriptor);
        System.out.println("[DEBUG] Return type: " + returnType);
        System.out.println("[DEBUG] Argument types: " + argumentTypes.length);
        for (int i = 0; i < argumentTypes.length; i++) {
            System.out.println("[DEBUG] arg$" + i + ": " + argumentTypes[i].getDescriptor());
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String internalName = lambdaClassName.replace('.', '/');
        String ownerInternalName = ownerClassName.replace('.', '/');

        // Definir la clase lambda que implementa Callable
        cw.visit(V1_8,
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
                internalName,
                null,
                "java/lang/Object",
                new String[]{"java/util/concurrent/Callable"});

        // Campos para almacenar parámetros
        generateFields(cw, ownerInternalName, argumentTypes);

        // Constructor
        generateConstructor(cw, internalName, ownerInternalName, argumentTypes);

        // Método call() de Callable
        generateCallMethod(cw, internalName, ownerInternalName, methodName, methodDescriptor, returnType, argumentTypes);

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void generateFields(ClassWriter cw, String ownerInternalName, Type[] argumentTypes) {
        // Campo para la instancia dueña
        cw.visitField(ACC_PRIVATE + ACC_FINAL, "this$0", "L" + ownerInternalName + ";", null, null);

        // Campos para cada argumento
        for (int i = 0; i < argumentTypes.length; i++) {
            cw.visitField(ACC_PRIVATE + ACC_FINAL, "arg$" + i, argumentTypes[i].getDescriptor(), null, null);
        }
    }

    private static void generateConstructor(ClassWriter cw, String lambdaInternalName,
            String ownerInternalName, Type[] argumentTypes) {

        String constructorDescriptor = getConstructorDescriptor(ownerInternalName, argumentTypes);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", constructorDescriptor, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        // Almacenar instancia dueña
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, lambdaInternalName, "this$0", "L" + ownerInternalName + ";");

        // Almacenar argumentos
        int varIndex = 2;
        for (int i = 0; i < argumentTypes.length; i++) {
            mv.visitVarInsn(ALOAD, 0);
            Type argType = argumentTypes[i];
            System.out.println("[LAMBDA GEN] Storing arg$" + i + " of type " + argType + " (" + argType.getDescriptor() + ") using opcode " + argType.getOpcode(ILOAD));
            // Usar el opcode correcto para cargar según el tipo de argumento
            mv.visitVarInsn(argType.getOpcode(ILOAD), varIndex);
            mv.visitFieldInsn(PUTFIELD, lambdaInternalName, "arg$" + i, argType.getDescriptor());
            varIndex += argType.getSize();
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(3, varIndex);
        mv.visitEnd();
    }

    private static void generateCallMethod(ClassWriter cw, String lambdaInternalName,
            String ownerInternalName, String methodName,
            String methodDescriptor, Type returnType,
            Type[] argumentTypes) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "call", "()Ljava/lang/Object;", null, null);
        mv.visitCode();

        // Cargar instancia dueña
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, lambdaInternalName, "this$0", "L" + ownerInternalName + ";");

        // Cargar argumentos usando el opcode correcto según el tipo
        for (int i = 0; i < argumentTypes.length; i++) {
            mv.visitVarInsn(ALOAD, 0); // Cargar referencia de la lambda
            Type argType = argumentTypes[i];
            System.out.println("[LAMBDA GEN] Loading arg$" + i + " of type " + argType + " (" + argType.getDescriptor() + ")");
            mv.visitFieldInsn(GETFIELD, lambdaInternalName, "arg$" + i, argType.getDescriptor());
            
            // Si el campo es de un tipo primitivo pero el método espera un tipo primitivo diferente,
            // puede haber un problema. Asegurarnos de que el tipo es correcto.
            // Los campos deberían contener el valor correcto del tipo declarado.
        }

        // Llamar al método original
        System.out.println("[DEBUG] About to call method: " + ownerInternalName + "." + methodName + methodDescriptor);
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerInternalName, methodName, methodDescriptor, false);

        // Boxear primitivos si es necesario
        if (isPrimitiveType(returnType) && returnType.getSort() != Type.VOID) {
            boxPrimitive(mv, returnType);
        }

        mv.visitInsn(ARETURN);
        mv.visitMaxs(argumentTypes.length + 2, 1);
        mv.visitEnd();
    }

    /**
     * Verifica si un tipo es primitivo
     */
    private static boolean isPrimitiveType(Type type) {
        return type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.DOUBLE;
    }

    private static void boxPrimitive(MethodVisitor mv, Type primitiveType) {
        switch (primitiveType.getSort()) {
            case Type.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case Type.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case Type.CHAR:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case Type.SHORT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case Type.INT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case Type.LONG:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case Type.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case Type.DOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
        }
    }

    private static String getConstructorDescriptor(String ownerInternalName, Type[] argumentTypes) {
        Type[] constructorArgs = new Type[argumentTypes.length + 1];
        constructorArgs[0] = Type.getObjectType(ownerInternalName);
        System.arraycopy(argumentTypes, 0, constructorArgs, 1, argumentTypes.length);
        return Type.getMethodDescriptor(Type.VOID_TYPE, constructorArgs);
    }
}
