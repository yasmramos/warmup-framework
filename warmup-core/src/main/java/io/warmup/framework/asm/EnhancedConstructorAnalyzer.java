package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.*;

class EnhancedConstructorAnalyzer extends MethodVisitor {

    private final String className;
    private final List<ConstructorMetadata> constructors;
    private final String descriptor;
    private final int access;
    private final List<String> parameterNames = new ArrayList<>();

    public EnhancedConstructorAnalyzer(String className, int access, String descriptor,
            List<ConstructorMetadata> constructors) {
        super(Opcodes.ASM9);
        this.className = className;
        this.descriptor = descriptor;
        this.constructors = constructors;
        this.access = access;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature,
            Label start, Label end, int index) {
        // First parameter starts at index 1 for instance methods (index 0 is 'this')
        if (index > 0 && index <= Type.getArgumentTypes(this.descriptor).length) {
            parameterNames.add(name);
        }
    }

    @Override
    public void visitEnd() {
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        String[] paramTypeNames = new String[argumentTypes.length];
        String[] paramInternalNames = new String[argumentTypes.length];

        // Ensure we have the right number of parameter names
        String[] resolvedParamNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            paramTypeNames[i] = argumentTypes[i].getClassName();
            paramInternalNames[i] = argumentTypes[i].getInternalName();
            resolvedParamNames[i] = i < parameterNames.size() ? parameterNames.get(i) : "arg" + i;
        }

        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        ConstructorMetadata metadata = new ConstructorMetadata(className, paramTypeNames,
                paramInternalNames, argumentTypes.length, isPublic);

        constructors.add(metadata);
    }
}
