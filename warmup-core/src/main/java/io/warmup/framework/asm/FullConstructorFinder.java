package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class FullConstructorFinder extends ClassVisitor {

    private final String className;
    private final List<ConstructorMetadata> constructors = new ArrayList<>();

    public FullConstructorFinder(String className) {
        super(Opcodes.ASM9);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature, String[] exceptions) {
        if (name.equals("<init>")) {
            return new AnnotatedConstructorAnalyzer(className, access, descriptor, constructors);
        }
        return null;
    }

    public List<ConstructorMetadata> getAllConstructors() {
        return constructors;
    }
}
