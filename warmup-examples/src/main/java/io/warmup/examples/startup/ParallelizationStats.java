package io.warmup.examples.startup;

/**
 * 📊 ESTADÍSTICAS DE PARALELIZACIÓN
 * 
 * Información detallada sobre el uso de paralelismo en el sistema de
 * inicialización paralela, incluyendo utilización de cores y eficiencia.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ParallelizationStats {
    
    private final int availableCores;
    private final int threadPoolSize;
    private final int subsystemCount;
    private final long totalParallelTimeNs;
    
    public ParallelizationStats(int availableCores, int threadPoolSize, 
                               int subsystemCount, long totalParallelTimeNs) {
        this.availableCores = availableCores;
        this.threadPoolSize = threadPoolSize;
        this.subsystemCount = subsystemCount;
        this.totalParallelTimeNs = totalParallelTimeNs;
    }
    
    /**
     * 🖥️ OBTENER NÚMERO DE CORES DISPONIBLES
     */
    public int getAvailableCores() {
        return availableCores;
    }
    
    /**
     * 🧵 OBTENER TAMAÑO DEL POOL DE THREADS
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    /**
     * 🔢 OBTENER NÚMERO DE SUBSISTEMAS
     */
    public int getSubsystemCount() {
        return subsystemCount;
    }
    
    /**
     * ⏱️ OBTENER TIEMPO TOTAL EN NANOSEGUNDOS
     */
    public long getTotalParallelTimeNs() {
        return totalParallelTimeNs;
    }
    
    /**
     * ⏱️ OBTENER TIEMPO TOTAL EN MILISEGUNDOS
     */
    public long getTotalParallelTimeMs() {
        return totalParallelTimeNs / 1_000_000;
    }
    
    /**
     * 📊 CALCULAR UTILIZACIÓN DE CORES (0.0 - 1.0)
     */
    public double getCoreUtilization() {
        if (availableCores <= 0) return 0.0;
        return Math.min(1.0, (double) threadPoolSize / availableCores);
    }
    
    /**
     * 📊 CALCULAR UTILIZACIÓN DE THREADS (0.0 - 1.0)
     */
    public double getThreadUtilization() {
        if (threadPoolSize <= 0) return 0.0;
        return Math.min(1.0, (double) subsystemCount / threadPoolSize);
    }
    
    /**
     * 🎯 VERIFICAR SI ESTÁ USANDO TODOS LOS CORES
     */
    public boolean isUsingAllCores() {
        return threadPoolSize >= availableCores;
    }
    
    /**
     * 🎯 VERIFICAR SI HAY SUFICIENTES THREADS PARA TODOS LOS SUBSISTEMAS
     */
    public boolean hasEnoughThreads() {
        return threadPoolSize >= subsystemCount;
    }
    
    /**
     * 📊 CALCULAR THREADS POR CORE
     */
    public double getThreadsPerCore() {
        if (availableCores <= 0) return 0.0;
        return (double) threadPoolSize / availableCores;
    }
    
    /**
     * 📊 CALCULAR CORES POR SUBSISTEMA
     */
    public double getCoresPerSubsystem() {
        if (subsystemCount <= 0) return 0.0;
        return (double) availableCores / subsystemCount;
    }
    
    /**
     * 🚀 CALCULAR IDEAL THREAD COUNT PARA LOS SUBSISTEMAS
     */
    public int getIdealThreadCount() {
        return Math.min(availableCores, Math.max(1, subsystemCount));
    }
    
    /**
     * 📊 CALCULAR OVERHEAD DE THREADING (estimado)
     */
    public double calculateThreadingOverhead() {
        if (threadPoolSize <= subsystemCount) return 0.0;
        
        // Estimación: 1% overhead por thread extra
        double extraThreads = threadPoolSize - Math.min(availableCores, subsystemCount);
        return extraThreads * 0.01;
    }
    
    /**
     * 🎯 OBTENER EFICIENCIA DE CONFIGURACIÓN (0.0 - 1.0)
     */
    public double getConfigurationEfficiency() {
        double coreEfficiency = getCoreUtilization();
        double threadEfficiency = getThreadUtilization();
        
        // Promedio ponderado: cores más importantes que threads
        return (coreEfficiency * 0.7) + (threadEfficiency * 0.3);
    }
    
    /**
     * 📈 OBTENER RECOMENDACIONES DE OPTIMIZACIÓN
     */
    public OptimizationRecommendations getRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        
        if (!isUsingAllCores()) {
            recommendations.append(String.format("• Aumentar threads a %d para usar todos los cores\n", availableCores));
        }
        
        if (!hasEnoughThreads()) {
            recommendations.append(String.format("• Aumentar threads a %d para paralelizar todos los subsistemas\n", subsystemCount));
        }
        
        if (threadPoolSize > availableCores * 2) {
            recommendations.append("• Considerar reducir threads (posible over-threading)\n");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("• Configuración de threads óptima\n");
        }
        
        return new OptimizationRecommendations(
            isUsingAllCores(),
            hasEnoughThreads(),
            getConfigurationEfficiency(),
            recommendations.toString()
        );
    }
    
    /**
     * 📊 GENERAR REPORTE DE ESTADÍSTICAS
     */
    public String generateStatsReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("🚀 ESTADÍSTICAS DE PARALELIZACIÓN\n");
        report.append("=================================\n\n");
        
        report.append("🖥️ RECURSOS DISPONIBLES:\n");
        report.append(String.format("  • Cores disponibles: %d\n", availableCores));
        report.append(String.format("  • Threads en pool: %d\n", threadPoolSize));
        report.append(String.format("  • Subsistemas: %d\n\n", subsystemCount));
        
        report.append("📊 UTILIZACIÓN:\n");
        report.append(String.format("  • Utilización de cores: %.1f%%\n", getCoreUtilization() * 100));
        report.append(String.format("  • Utilización de threads: %.1f%%\n", getThreadUtilization() * 100));
        report.append(String.format("  • Threads por core: %.2f\n", getThreadsPerCore()));
        report.append(String.format("  • Cores por subsistema: %.2f\n\n", getCoresPerSubsystem()));
        
        report.append("⚙️ CONFIGURACIÓN:\n");
        report.append(String.format("  • Usando todos los cores: %s\n", isUsingAllCores() ? "✅ Sí" : "❌ No"));
        report.append(String.format("  • Threads suficientes: %s\n", hasEnoughThreads() ? "✅ Sí" : "❌ No"));
        report.append(String.format("  • Eficiencia de config: %.1f%%\n\n", getConfigurationEfficiency() * 100));
        
        report.append("⏱️ TIEMPO:\n");
        report.append(String.format("  • Tiempo total paralelo: %dms\n", getTotalParallelTimeMs()));
        
        OptimizationRecommendations recommendations = getRecommendations();
        report.append("🎯 RECOMENDACIONES:\n");
        report.append(recommendations.getRecommendationsText());
        
        return report.toString();
    }
    
    /**
     * 📊 CLASE PARA RECOMENDACIONES DE OPTIMIZACIÓN
     */
    public static class OptimizationRecommendations {
        private final boolean usingAllCores;
        private final boolean hasEnoughThreads;
        private final double efficiency;
        private final String recommendations;
        
        public OptimizationRecommendations(boolean usingAllCores, boolean hasEnoughThreads, 
                                         double efficiency, String recommendations) {
            this.usingAllCores = usingAllCores;
            this.hasEnoughThreads = hasEnoughThreads;
            this.efficiency = efficiency;
            this.recommendations = recommendations;
        }
        
        public boolean isUsingAllCores() { return usingAllCores; }
        public boolean isHasEnoughThreads() { return hasEnoughThreads; }
        public double getEfficiency() { return efficiency; }
        public String getRecommendationsText() { return recommendations; }
    }
    
    @Override
    public String toString() {
        return generateStatsReport();
    }
}