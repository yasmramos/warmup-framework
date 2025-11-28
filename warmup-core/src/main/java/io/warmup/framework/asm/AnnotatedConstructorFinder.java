package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AnnotatedConstructorFinder extends ClassVisitor {
    private final String className;
    private final String targetAnnotation;
    private ConstructorMetadata annotatedConstructor;

    public AnnotatedConstructorFinder(String className, String targetAnnotation) {
        super(Opcodes.ASM9);
        this.className = className;
        this.targetAnnotation = targetAnnotation;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<init>")) {
            return new AnnotationCheckingConstructorAnalyzer(className, access, descriptor, targetAnnotation, this);
        }
        return null;
    }

    public void setAnnotatedConstructor(ConstructorMetadata metadata) {
        this.annotatedConstructor = metadata;
    }

    public ConstructorMetadata getAnnotatedConstructor() {
        return annotatedConstructor;
    }
}