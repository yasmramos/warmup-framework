package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Lazy;
import java.util.HashSet;
import java.util.Set;

@Lazy
@Component(singleton = true)
public class HeavyService {

    private final String id;
    private static int instanceCount = 0;
    private static final Object lock = new Object();
    private static final Set<String> instanceIds = new HashSet<>();

    public HeavyService() {
        synchronized (lock) {
            instanceCount++;
            this.id = "HeavyService-" + System.currentTimeMillis() + "-inst" + instanceCount;
        }

        // ✅ Verificar stack trace para ver quién está creando la instancia
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean fromProxy = false;
        boolean fromValidation = false;

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("_$$_jvst")) {
                fromProxy = true;
            }
            if (element.getMethodName().contains("createDummyInstance")) {
                fromValidation = true;
            }
        }

        System.out.println("CREANDO INSTANCIA " + instanceCount + ": " + this.id);
        System.out.println("   Desde proxy: " + fromProxy + ", Desde validación: " + fromValidation);

        if (fromValidation) {
            System.out.println("   ❌ ERROR: Instancia creada durante validación");
        }
        if (fromProxy && !stackTrace[2].getMethodName().equals("invoke")) {
            System.out.println("   ❌ ERROR: Instancia creada durante construcción del proxy");
        }

        // Simular inicialización costosa
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        System.out.println("✅ " + this.id + " LISTO");
    }

    public void process() {
        System.out.println("🔄 " + id + " procesando...");
    }

    public String getId() {
        return id;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

    public static void resetInstanceCount() {
        instanceCount = 0;
    }
}
