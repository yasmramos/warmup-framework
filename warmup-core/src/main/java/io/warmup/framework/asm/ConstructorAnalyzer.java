package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import java.util.List;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class ConstructorAnalyzer extends MethodVisitor {

    private final String className;
    private final List<ConstructorMetadata> constructors;
    private final String descriptor;
    private final int access;

    public ConstructorAnalyzer(String className, int access, String descriptor,
            List<ConstructorMetadata> constructors) {
        super(Opcodes.ASM9);
        this.className = className;
        this.descriptor = descriptor;
        this.constructors = constructors;
        this.access = access;
    }

    @Override
    public void visitCode() {
        // Analizar tipos de parámetros del descriptor
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        String[] paramTypeNames = new String[argumentTypes.length];
        String[] paramInternalNames = new String[argumentTypes.length];

        for (int i = 0; i < argumentTypes.length; i++) {
            paramTypeNames[i] = argumentTypes[i].getClassName();
            paramInternalNames[i] = argumentTypes[i].getInternalName();
        }

        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        constructors.add(new ConstructorMetadata(className, paramTypeNames,
                paramInternalNames, argumentTypes.length, isPublic));
    }
}
