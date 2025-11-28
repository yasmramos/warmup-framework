import io.warmup.framework.core.WarmupContainer;
import java.util.concurrent.TimeUnit;

public class SimpleWarmupTest {
    
    public static class TestBean {
        private String name;
        private int value;
        
        public TestBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO BENCHMARK REAL - WARMUPCONTAINER");
        System.out.println("=" * 60);
        
        // Configuración baseline - sin optimizaciones extremas
        System.setProperty("warmup.startup.parallel", "false");
        System.setProperty("warmup.startup.unsafe", "false");
        System.setProperty("warmup.startup.critical", "false");
        System.setProperty("warmup.startup.gc", "false");
        System.setProperty("warmup.startup.analysis", "false");
        
        // Benchmark de creación de container
        System.out.println("📊 Ejecutando 10 iteraciones...");
        
        long[] times = new long[10];
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            
            try (WarmupContainer container = new WarmupContainer()) {
                // Registro básico
                container.registerBean("testBean", new TestBean("Test" + i, i));
                
                // Inicialización
                container.initialize();
            }
            
            long end = System.nanoTime();
            times[i] = (end - start) / 1_000_000; // Convertir a ms
            System.out.printf("  Iteración %d: %dms%n", i + 1, times[i]);
        }
        
        // Calcular estadísticas
        long sum = 0;
        long min = times[0];
        long max = times[0];
        
        for (long time : times) {
            sum += time;
            min = Math.min(min, time);
            max = Math.max(max, time);
        }
        
        double average = (double) sum / times.length;
        
        System.out.println("\n📈 RESULTADOS DEL BENCHMARK:");
        System.out.println("=" * 40);
        System.out.printf("Promedio: %.2fms%n", average);
        System.out.printf("Mínimo: %dms%n", min);
        System.out.printf("Máximo: %dms%n", max);
        System.out.printf("Total: %dms%n", sum);
        
        // Benchmark con beans múltiples
        System.out.println("\n🔄 BENCHMARK CON 50 BEANS:");
        long startMulti = System.nanoTime();
        
        try (WarmupContainer container = new WarmupContainer()) {
            for (int i = 0; i < 50; i++) {
                container.registerBean("bean_" + i, new TestBean("Bean" + i, i));
            }
            container.initialize();
        }
        
        long endMulti = System.nanoTime();
        long multiTime = (endMulti - startMulti) / 1_000_000;
        System.out.printf("Tiempo con 50 beans: %dms%n", multiTime);
        
        System.out.println("\n✅ BENCHMARK COMPLETADO EXITOSAMENTE");
    }
}
