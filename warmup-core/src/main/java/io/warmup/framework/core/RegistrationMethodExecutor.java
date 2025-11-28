package io.warmup.framework.core;

import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Ejecutor del método de registro - VERSIÓN QUE REALMENTE EJECUTA
 */
class RegistrationMethodExecutor extends MethodVisitor {

    private final WarmupContainer container;
    private boolean executed = false;

    public RegistrationMethodExecutor(WarmupContainer container) {
        super(Opcodes.ASM9);
        this.container = container;
    }

    @Override
    public void visitCode() {
        // EJECUTAR INMEDIATAMENTE usando reflexión como fallback seguro
        if (!executed) {
            executeWithReflection();
            executed = true;
        }
    }

    private void executeWithReflection() {
        try {
            System.out.println("🔧 Ejecutando registro pre-compilado...");
            // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión (null = static method)
            Object result = AsmCoreUtils.invokeMethod(null, "registerAllComponents", container);
            System.out.println("✅ Registro pre-compilado completado");
        } catch (Exception e) {
            System.err.println("❌ Error en registro pre-compilado: " + e.getMessage());
        }
    }

    @Override
    public void visitInsn(int opcode) {
        // Detectar cuando el método termina (RETURN)
        if (opcode == Opcodes.RETURN && !executed) {
            executeWithReflection();
            executed = true;
        }
    }
}
