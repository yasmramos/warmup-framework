package io.warmup.framework.proxy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * ClassLoader para cargar las clases transformadas con ASM puro
 */
public class AsyncClassLoader extends ClassLoader implements Opcodes {

    public AsyncClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Define una clase desde bytecode sin usar reflexión
     */
    public Class<?> defineClass(String name, byte[] bytecode) {
        return defineClass(name, bytecode, 0, bytecode.length, getClass().getProtectionDomain());
    }

    /**
     * Transforma una clase aplicando la transformación asincrónica
     */
    public byte[] transformClass(String className, byte[] originalBytecode) {
        ClassReader classReader = new ClassReader(originalBytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        
        AsyncClassVisitor transformer = new AsyncClassVisitor(classWriter, originalBytecode, this);
        classReader.accept(transformer, 0); // Usar 0 en lugar de ClassReader.EXPAND_FRAMES
        
        return classWriter.toByteArray();
    }
}