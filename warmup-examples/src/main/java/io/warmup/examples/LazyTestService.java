package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;

@Component
public class LazyTestService {

    @Inject
    private HeavyService heavyService;

    public void runTest() {
        System.out.println("=== TEST @Lazy ===");
        System.out.println("Tipo: " + heavyService.getClass().getSimpleName());
        System.out.println("Contador de instancias: " + HeavyService.getInstanceCount());

        System.out.println("Llamando por primera vez...");
        heavyService.process();

        System.out.println("Llamando por segunda vez...");
        heavyService.process();

        System.out.println("ID: " + heavyService.getId());
        System.out.println("Total instancias creadas: " + HeavyService.getInstanceCount());
    }
}
