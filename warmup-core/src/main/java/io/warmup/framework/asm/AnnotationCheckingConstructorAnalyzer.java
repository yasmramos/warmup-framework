package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import java.util.Arrays;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class AnnotationCheckingConstructorAnalyzer extends MethodVisitor {

    private final String className;
    private final String descriptor;
    private final int access;
    private final String targetAnnotation;
    private final AnnotatedConstructorFinder finder;
    private boolean hasTargetAnnotation = false;

    public AnnotationCheckingConstructorAnalyzer(String className, int access, String descriptor,
            String targetAnnotation, AnnotatedConstructorFinder finder) {
        super(Opcodes.ASM9);
        this.className = className;
        this.descriptor = descriptor;
        this.access = access;
        this.targetAnnotation = targetAnnotation;
        this.finder = finder;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(targetAnnotation)) {
            hasTargetAnnotation = true;
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (hasTargetAnnotation) {
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);
            String[] paramTypeNames = Arrays.stream(argumentTypes)
                    .map(Type::getClassName)
                    .toArray(String[]::new);
            String[] paramInternalNames = Arrays.stream(argumentTypes)
                    .map(Type::getInternalName)
                    .toArray(String[]::new);

            boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
            ConstructorMetadata metadata = new ConstructorMetadata(className, paramTypeNames,
                    paramInternalNames, argumentTypes.length, isPublic);
            finder.setAnnotatedConstructor(metadata);
        }
    }
}
