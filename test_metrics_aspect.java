import io.warmup.framework.metrics.MetricsAspect;
import io.warmup.framework.metrics.MethodMetrics;
import io.warmup.framework.core.WarmupContainer;

public class test_metrics_aspect {
    public static void main(String[] args) {
        try {
            System.out.println("Testing MetricsAspect creation...");
            
            WarmupContainer container = new WarmupContainer();
            MethodMetrics methodMetrics = new MethodMetrics();
            
            // Test constructor with parameters
            MetricsAspect aspect = new MetricsAspect(container, methodMetrics);
            
            System.out.println("✅ MetricsAspect created successfully!");
            System.out.println("Aspect instance: " + aspect);
            System.out.println("MethodMetrics: " + aspect.methodMetrics);
            System.out.println("Container: " + aspect.container);
            
        } catch (Exception e) {
            System.out.println("❌ Error creating MetricsAspect:");
            e.printStackTrace();
        }
    }
}