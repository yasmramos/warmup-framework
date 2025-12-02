package io.warmup.framework.startup.lazy;

import io.warmup.framework.core.DependencyRegistry;
import io.warmup.framework.core.WarmupContainer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🗂️ REGISTRO DE BEANS LAZY
 * 
 * Gestiona el registro y acceso a beans que se crean solo cuando se solicitan.
 * Proporciona:
 * - ✅ Registro de beans lazy con sus suppliers
 * - ✅ Acceso thread-safe a beans lazy
 * - ✅ Inyección de dependencias automática
 * - ✅ Caching y reutilización de instancias
 * - ✅ Estadísticas y monitoreo de uso
 * - ✅ Cleanup y shutdown de beans
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class LazyBeanRegistry {
    
    private static final Logger log = Logger.getLogger(LazyBeanRegistry.class.getName());
    
    private final WarmupContainer container;
    private final DependencyRegistry dependencyRegistry;
    
    // 📊 REGISTROS PRINCIPALES
    private final Map<String, LazyBeanSupplier<?>> lazyBeans = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> beanTypes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> beanDependencies = new ConcurrentHashMap<>();
    
    // 📈 ESTADÍSTICAS GLOBALES
    private final AtomicInteger totalRegisteredBeans = new AtomicInteger(0);
    private final AtomicInteger totalCreatedBeans = new AtomicInteger(0);
    private final AtomicInteger totalBeanAccesses = new AtomicInteger(0);
    private final AtomicInteger totalBeanErrors = new AtomicInteger(0);
    
    public LazyBeanRegistry(WarmupContainer container, DependencyRegistry dependencyRegistry) {
        this.container = container;
        this.dependencyRegistry = dependencyRegistry;
    }
    
    /**
     * 📝 REGISTRAR BEAN LAZY
     */
    public <T> void registerLazyBean(String beanName, Class<T> beanType, LazyBeanSupplier<T> supplier) {
        if (beanName == null || beanName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bean name cannot be null or empty");
        }
        
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null for bean: " + beanName);
        }
        
        log.log(Level.FINE, "📝 Registrando bean lazy: {0} (tipo: {1})", 
                new Object[]{beanName, beanType.getSimpleName()});
        
        lazyBeans.put(beanName, supplier);
        beanTypes.put(beanName, beanType);
        totalRegisteredBeans.incrementAndGet();
        
        // Registrar en DependencyRegistry como supplier
        if (dependencyRegistry != null) {
            dependencyRegistry.registerWithSupplier(beanType, supplier, true);
        }
    }
    
    /**
     * 📝 REGISTRAR BEAN LAZY CON SUPPLIER SIMPLE
     */
    public <T> void registerLazyBean(String beanName, Class<T> beanType, java.util.function.Supplier<T> realSupplier) {
        LazyBeanSupplier<T> lazySupplier = new LazyBeanSupplier<>(beanName, realSupplier);
        registerLazyBean(beanName, beanType, lazySupplier);
    }
    
    /**
     * 🎯 OBTENER BEAN LAZY
     */
    @SuppressWarnings("unchecked")
    public <T> T getLazyBean(String beanName, Class<T> expectedType) {
        if (beanName == null) {
            throw new IllegalArgumentException("Bean name cannot be null");
        }
        
        LazyBeanSupplier<T> supplier = (LazyBeanSupplier<T>) lazyBeans.get(beanName);
        if (supplier == null) {
            throw new IllegalArgumentException("Lazy bean not registered: " + beanName);
        }
        
        // Verificar tipo si se especifica
        if (expectedType != null) {
            Class<?> registeredType = beanTypes.get(beanName);
            if (registeredType != null && !expectedType.isAssignableFrom(registeredType)) {
                throw new ClassCastException(String.format(
                    "Bean %s is of type %s but requested as %s", 
                    beanName, registeredType.getName(), expectedType.getName()
                ));
            }
        }
        
        totalBeanAccesses.incrementAndGet();
        
        try {
            T bean = supplier.get();
            totalCreatedBeans.incrementAndGet();
            return bean;
        } catch (Exception e) {
            totalBeanErrors.incrementAndGet();
            log.log(Level.SEVERE, "❌ Error obteniendo bean lazy {0}: {1}", 
                    new Object[]{beanName, e.getMessage()});
            throw e;
        }
    }
    
    /**
     * 🎯 OBTENER BEAN LAZY SIN VERIFICACIÓN DE TIPO
     */
    @SuppressWarnings("unchecked")
    public <T> T getLazyBean(String beanName) {
        return getLazyBean(beanName, null);
    }
    
    /**
     * ✅ VERIFICAR SI UN BEAN ESTÁ REGISTRADO
     */
    public boolean isBeanRegistered(String beanName) {
        return lazyBeans.containsKey(beanName);
    }
    
    /**
     * ✅ VERIFICAR SI UN BEAN YA FUE CREADO
     */
    public boolean isBeanCreated(String beanName) {
        LazyBeanSupplier<?> supplier = lazyBeans.get(beanName);
        return supplier != null && supplier.isCreated();
    }
    
    /**
     * ❌ VERIFICAR SI UN BEAN TIENE ERROR
     */
    public boolean hasBeanError(String beanName) {
        LazyBeanSupplier<?> supplier = lazyBeans.get(beanName);
        return supplier != null && supplier.hasError();
    }
    
    /**
     * 📊 OBTENER TIPO DE UN BEAN
     */
    public Class<?> getBeanType(String beanName) {
        return beanTypes.get(beanName);
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DE UN BEAN
     */
    public LazyBeanSupplier.LazyBeanStats getBeanStats(String beanName) {
        LazyBeanSupplier<?> supplier = lazyBeans.get(beanName);
        if (supplier == null) {
            throw new IllegalArgumentException("Bean not registered: " + beanName);
        }
        return supplier.getStats();
    }
    
    /**
     * 📊 OBTENER TODAS LAS ESTADÍSTICAS
     */
    public Map<String, LazyBeanSupplier.LazyBeanStats> getAllBeanStats() {
        Map<String, LazyBeanSupplier.LazyBeanStats> stats = new HashMap<>();
        for (String beanName : lazyBeans.keySet()) {
            stats.put(beanName, getBeanStats(beanName));
        }
        return stats;
    }
    
    /**
     * 📈 OBTENER ESTADÍSTICAS GLOBALES
     */
    public GlobalLazyStats getGlobalStats() {
        return new GlobalLazyStats(
            totalRegisteredBeans.get(),
            totalCreatedBeans.get(),
            totalBeanAccesses.get(),
            totalBeanErrors.get(),
            lazyBeans.size()
        );
    }
    
    /**
     * 🔍 LISTAR TODOS LOS BEANS REGISTRADOS
     */
    public List<String> listRegisteredBeans() {
        return new ArrayList<>(lazyBeans.keySet());
    }
    
    /**
     * 🔍 LISTAR BEANS YA CREADOS
     */
    public List<String> listCreatedBeans() {
        return lazyBeans.entrySet().stream()
            .filter(entry -> entry.getValue().isCreated())
            .map(Map.Entry::getKey)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * 🔍 LISTAR BEANS CON ERROR
     */
    public List<String> listBeansWithErrors() {
        return lazyBeans.entrySet().stream()
            .filter(entry -> entry.getValue().hasError())
            .map(Map.Entry::getKey)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * 🧹 LIMPIAR BEAN ESPECÍFICO
     */
    public void clearBean(String beanName) {
        LazyBeanSupplier<?> supplier = lazyBeans.remove(beanName);
        if (supplier != null) {
            beanTypes.remove(beanName);
            beanDependencies.remove(beanName);
            log.log(Level.FINE, "🧹 Bean lazy removido: {0}", beanName);
        }
    }
    
    /**
     * 🧹 LIMPIAR TODOS LOS BEANS
     */
    public void clearAllBeans() {
        lazyBeans.clear();
        beanTypes.clear();
        beanDependencies.clear();
        
        log.log(Level.INFO, "🧹 Todos los beans lazy han sido removidos");
    }
    
    /**
     * 📊 GENERAR REPORTE DE ESTADÍSTICAS
     */
    public String generateStatsReport() {
        StringBuilder report = new StringBuilder();
        
        GlobalLazyStats globalStats = getGlobalStats();
        
        report.append("📊 REPORTE DE BEANS LAZY\n");
        report.append("=========================\n\n");
        
        report.append("📈 ESTADÍSTICAS GLOBALES:\n");
        report.append(String.format("  • Beans registrados: %d\n", globalStats.getRegisteredBeans()));
        report.append(String.format("  • Beans creados: %d\n", globalStats.getCreatedBeans()));
        report.append(String.format("  • Total accesos: %d\n", globalStats.getTotalAccesses()));
        report.append(String.format("  • Errores totales: %d\n", globalStats.getTotalErrors()));
        report.append(String.format("  • Tasa de creación: %.1f%%\n", 
                globalStats.getCreationRate() * 100));
        
        if (globalStats.getCreatedBeans() > 0) {
            double avgAccessesPerBean = (double) globalStats.getTotalAccesses() / globalStats.getCreatedBeans();
            report.append(String.format("  • Promedio accesos por bean: %.1f\n", avgAccessesPerBean));
        }
        
        report.append("\n📋 DETALLE POR BEAN:\n");
        report.append("--------------------\n");
        
        for (Map.Entry<String, LazyBeanSupplier<?>> entry : lazyBeans.entrySet()) {
            String beanName = entry.getKey();
            LazyBeanSupplier.LazyBeanStats stats = entry.getValue().getStats();
            
            String status = stats.isSuccessfullyCreated() ? "✅" : (stats.hasError() ? "❌" : "⏳");
            report.append(String.format("  %s %s: %s\n", status, beanName, stats));
        }
        
        return report.toString();
    }
    
    /**
     * 📊 CLASE PARA ESTADÍSTICAS GLOBALES
     */
    public static class GlobalLazyStats {
        private final int registeredBeans;
        private final int createdBeans;
        private final int totalAccesses;
        private final int totalErrors;
        private final int activeBeans;
        
        public GlobalLazyStats(int registeredBeans, int createdBeans, 
                             int totalAccesses, int totalErrors, int activeBeans) {
            this.registeredBeans = registeredBeans;
            this.createdBeans = createdBeans;
            this.totalAccesses = totalAccesses;
            this.totalErrors = totalErrors;
            this.activeBeans = activeBeans;
        }
        
        public int getRegisteredBeans() { return registeredBeans; }
        public int getCreatedBeans() { return createdBeans; }
        public int getTotalAccesses() { return totalAccesses; }
        public int getTotalErrors() { return totalErrors; }
        public int getActiveBeans() { return activeBeans; }
        
        public double getCreationRate() {
            return registeredBeans > 0 ? (double) createdBeans / registeredBeans : 0.0;
        }
        
        public double getErrorRate() {
            return createdBeans > 0 ? (double) totalErrors / createdBeans : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("GlobalLazyStats{registered=%d, created=%d, accesses=%d, errors=%d}",
                    registeredBeans, createdBeans, totalAccesses, totalErrors);
        }
    }
}