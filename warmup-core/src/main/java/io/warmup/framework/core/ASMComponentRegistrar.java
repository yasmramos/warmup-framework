package io.warmup.framework.core;

import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Registrador de componentes usando ASM con sistema de caché
 */
public class ASMComponentRegistrar extends ClassVisitor {

    private static final Logger log = Logger.getLogger(ASMComponentRegistrar.class.getName());

    private final WarmupContainer container;
    private final ASMCacheManager cacheManager;
    private boolean successful = false;
    private String sourceHash;

    public ASMComponentRegistrar(WarmupContainer container) {
        super(Opcodes.ASM9);
        this.container = container;
        this.cacheManager = ASMCacheManager.getInstance();
    }

    /**
     * Cargar componentes pre-compilados con caché
     */
    public static boolean loadPrecompiledWithCache(WarmupContainer container) {
        String configClass = "io.warmup.framework.generated.PrecompiledComponentConfig";
        String configClassPath = "io/warmup/framework/generated/PrecompiledComponentConfig.class";

        ASMCacheManager cacheManager = ASMCacheManager.getInstance();

        // Use try-with-resources for automatic resource management
        try (InputStream classStream = ASMComponentRegistrar.class.getClassLoader().getResourceAsStream(configClassPath)) {

            if (classStream == null) {
                log.fine("No se encontró clase pre-compilada");
                return false;
            }

            // 1. Leer el bytecode original (Java 8 compatible)
            byte[] originalBytecode = readAllBytesJava8(classStream);

            // 2. Calcular hash del bytecode ORIGINAL (corregido)
            String sourceHash = cacheManager.calculateSourceHash(originalBytecode);

            log.log(Level.INFO, " Hash del bytecode: {0}...", sourceHash.substring(0, 16));

            // 3. Buscar en caché
            byte[] cachedBytecode = cacheManager.getCachedBytecode(configClass, sourceHash);

            if (cachedBytecode != null) {
                // CACHE HIT - Usar bytecode cacheado
                log.log(Level.INFO, "Usando bytecode cacheado para {0}", configClass);
                return executeFromCachedBytecode(container, cachedBytecode);
            }

            // CACHE MISS - Procesar con ASM
            log.info("⚙️  Procesando con ASM (no hay caché)...");
            ClassReader classReader = new ClassReader(originalBytecode);
            ASMComponentRegistrar registrar = new ASMComponentRegistrar(container);
            registrar.sourceHash = sourceHash;

            classReader.accept(registrar, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            if (registrar.wasSuccessful()) {
                // 4. Cachear el bytecode procesado (si ASM lo modificó)
                cacheManager.cacheBytecode(configClass, sourceHash, originalBytecode);
                log.info("💾 Bytecode cacheado para futuras ejecuciones");
                return true;
            }

            return false;

        } catch (IOException e) {
            log.log(Level.WARNING, "Error cargando con caché: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Java 8 compatible method to read all bytes from an InputStream
     */
    private static byte[] readAllBytesJava8(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096]; // 4KB buffer
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * Ejecutar registro desde bytecode cacheado
     */
    private static boolean executeFromCachedBytecode(WarmupContainer container, byte[] bytecode) {
        try {
            // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión (null = static method)
            Object result = AsmCoreUtils.invokeMethod(null, "registerAllComponents", container);

            log.info("Componentes registrados desde caché");
            return true;

        } catch (Exception e) {
            log.log(Level.WARNING, "Error ejecutando desde caché: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature, String[] exceptions) {

        if ("registerAllComponents".equals(name)) {
            log.fine("Encontrado método registerAllComponents");
            return new RegistrationMethodVisitor(container);
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        successful = true;
        super.visitEnd();
    }

    public boolean wasSuccessful() {
        return successful;
    }

    /**
     * Visitor para el método de registro
     */
    private static class RegistrationMethodVisitor extends MethodVisitor {

        private final WarmupContainer container;
        private boolean executed = false;

        public RegistrationMethodVisitor(WarmupContainer container) {
            super(Opcodes.ASM9);
            this.container = container;
        }

        @Override
        public void visitCode() {
            if (!executed) {
                executeRegistration();
                executed = true;
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN && !executed) {
                executeRegistration();
                executed = true;
            }
        }

        private void executeRegistration() {
            try {
                log.info("Ejecutando registro de componentes...");
                // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión (null = static method)
                Object result = AsmCoreUtils.invokeMethod(null, "registerAllComponents", container);
                log.info("Registro completado");
            } catch (Exception e) {
                log.log(Level.SEVERE, " Error ejecutando registro: " + e.getMessage(), e);
            }
        }
    }
}
