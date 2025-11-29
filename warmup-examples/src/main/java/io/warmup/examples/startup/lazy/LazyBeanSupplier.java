package io.warmup.examples.startup.lazy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 SUPPLIER LAZY PARA CREACIÓN DE BEANS ON-DEMAND
 * 
 * Envuelve cualquier bean en un supplier que solo crea la instancia
 * cuando se solicita por primera vez, eliminando completamente el costo
 * de startup para beans no utilizados.
 * 
 * Características:
 * - ✅ Creación lazy: Solo se ejecuta cuando se solicita
 * - ✅ Thread-safe: Múltiples threads pueden solicitar simultáneamente
 * - ✅ Caching: Se crea una sola vez y se reutiliza
 * - ✅ Lazy loading: Sin costo de startup para beans no usados
 * - ✅ Error handling: Captura y cachea errores de creación
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class LazyBeanSupplier<T> implements Supplier<T> {
    
    private static final Logger log = Logger.getLogger(LazyBeanSupplier.class.getName());
    
    private final String beanName;
    private final Supplier<T> realSupplier;
    private final AtomicReference<T> cachedInstance = new AtomicReference<>();
    private final AtomicReference<Exception> cachedError = new AtomicReference<>();
    private final ReentrantLock creationLock = new ReentrantLock();
    private final boolean isEager;
    private volatile boolean isCreated = false;
    
    // Estadísticas
    private final AtomicReference<Long> creationTimeNs = new AtomicReference<>();
    private final AtomicInteger accessCount = new AtomicInteger(0);
    private final AtomicInteger creationCount = new AtomicInteger(0);
    
    public LazyBeanSupplier(String beanName, Supplier<T> realSupplier) {
        this(beanName, realSupplier, false);
    }
    
    public LazyBeanSupplier(String beanName, Supplier<T> realSupplier, boolean isEager) {
        this.beanName = beanName;
        this.realSupplier = realSupplier;
        this.isEager = isEager;
        
        if (isEager) {
            // Eager initialization: crear inmediatamente
            get();
        }
    }
    
    /**
     * 🎯 OBTENER BEAN - Crea solo en el primer uso
     */
    @Override
    public T get() {
        accessCount.incrementAndGet();
        
        // Intentar obtener del cache
        T cached = cachedInstance.get();
        if (cached != null) {
            log.log(Level.FINEST, "🔄 Bean {0} obtenido del cache (acceso #{1})", 
                    new Object[]{beanName, accessCount.get()});
            return cached;
        }
        
        // Verificar si hay error cacheado
        Exception error = cachedError.get();
        if (error != null) {
            log.log(Level.WARNING, "⚠️ Bean {0} tiene error cacheado: {1}", 
                    new Object[]{beanName, error.getMessage()});
            throw new LazyBeanCreationException("Bean " + beanName + " failed to create", error);
        }
        
        // Crear nueva instancia (thread-safe)
        return createIfNeeded();
    }
    
    /**
     * 🔧 CREAR INSTANCIA SI ES NECESARIO (Thread-safe)
     */
    private T createIfNeeded() {
        // Double-checked locking pattern para thread safety y performance
        T cached = cachedInstance.get();
        if (cached != null || cachedError.get() != null) {
            return cachedInstance.get(); // Puede ser null si hay error
        }
        
        creationLock.lock();
        try {
            // Verificar de nuevo dentro del lock
            cached = cachedInstance.get();
            if (cached != null || cachedError.get() != null) {
                return cachedInstance.get();
            }
            
            // Crear la instancia
            long startTime = System.nanoTime();
            
            try {
                log.log(Level.FINE, "🏗️ Creando bean lazy: {0}", beanName);
                
                T newInstance = realSupplier.get();
                boolean success = cachedInstance.compareAndSet(null, newInstance);
                
                long duration = System.nanoTime() - startTime;
                creationTimeNs.set(duration);
                creationCount.incrementAndGet();
                isCreated = true;
                
                if (success) {
                    log.log(Level.FINE, "✅ Bean {0} creado exitosamente en {1}μs (acceso #{2})", 
                            new Object[]{beanName, duration / 1_000, accessCount.get()});
                    return newInstance;
                } else {
                    // Otro thread ya lo creó, obtener del cache
                    return cachedInstance.get();
                }
                
            } catch (Exception e) {
                long duration = System.nanoTime() - startTime;
                cachedError.set(e);
                creationTimeNs.set(duration);
                creationCount.incrementAndGet();
                
                log.log(Level.SEVERE, "❌ Error creando bean lazy {0} después de {1}μs: {2}", 
                        new Object[]{beanName, duration / 1_000, e.getMessage()});
                
                throw new LazyBeanCreationException("Failed to create bean " + beanName, e);
            }
            
        } finally {
            creationLock.unlock();
        }
    }
    
    /**
     * ✅ VERIFICAR SI EL BEAN YA FUE CREADO
     */
    public boolean isCreated() {
        return isCreated || cachedInstance.get() != null || cachedError.get() != null;
    }
    
    /**
     * ✅ VERIFICAR SI EL BEAN FUE CREADO EXITOSAMENTE
     */
    public boolean isSuccessfullyCreated() {
        return cachedInstance.get() != null;
    }
    
    /**
     * ❌ VERIFICAR SI HAY ERROR DE CREACIÓN
     */
    public boolean hasError() {
        return cachedError.get() != null;
    }
    
    /**
     * ❌ OBTENER ERROR DE CREACIÓN
     */
    public Exception getCreationError() {
        return cachedError.get();
    }
    
    /**
     * ⏱️ OBTENER TIEMPO DE CREACIÓN
     */
    public long getCreationTimeNs() {
        Long time = creationTimeNs.get();
        return time != null ? time : 0;
    }
    
    /**
     * ⏱️ OBTENER TIEMPO DE CREACIÓN EN MILISEGUNDOS
     */
    public long getCreationTimeMs() {
        return getCreationTimeNs() / 1_000_000;
    }
    
    /**
     * 🔢 OBTENER NÚMERO DE ACCESOS
     */
    public int getAccessCount() {
        return accessCount.get();
    }
    
    /**
     * 🔢 OBTENER NÚMERO DE VECES QUE SE INTENTÓ CREAR
     */
    public int getCreationCount() {
        return creationCount.get();
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DEL BEAN
     */
    public LazyBeanStats getStats() {
        return new LazyBeanStats(
            beanName,
            isSuccessfullyCreated(),
            hasError(),
            getCreationTimeMs(),
            getAccessCount(),
            getCreationCount()
        );
    }
    
    /**
     * 🔄 FORZAR RECREACIÓN (útil para testing)
     */
    public T recreate() {
        creationLock.lock();
        try {
            cachedInstance.set(null);
            cachedError.set(null);
            isCreated = false;
            creationTimeNs.set(null);
            
            log.log(Level.FINE, "🔄 Forzando recreación de bean: {0}", beanName);
            return get();
        } finally {
            creationLock.unlock();
        }
    }
    
    /**
     * 📊 CLASE PARA ESTADÍSTICAS DEL BEAN LAZY
     */
    public static class LazyBeanStats {
        private final String beanName;
        private final boolean successfullyCreated;
        private final boolean hasError;
        private final long creationTimeMs;
        private final int accessCount;
        private final int creationCount;
        
        public LazyBeanStats(String beanName, boolean successfullyCreated, boolean hasError,
                           long creationTimeMs, int accessCount, int creationCount) {
            this.beanName = beanName;
            this.successfullyCreated = successfullyCreated;
            this.hasError = hasError;
            this.creationTimeMs = creationTimeMs;
            this.accessCount = accessCount;
            this.creationCount = creationCount;
        }
        
        public String getBeanName() { return beanName; }
        public boolean isSuccessfullyCreated() { return successfullyCreated; }
        public boolean hasError() { return hasError; }
        public long getCreationTimeMs() { return creationTimeMs; }
        public int getAccessCount() { return accessCount; }
        public int getCreationCount() { return creationCount; }
        
        @Override
        public String toString() {
            return String.format("LazyBeanStats{bean='%s', created=%s, error=%s, time=%dms, accesses=%d, creations=%d}",
                    beanName, successfullyCreated, hasError, creationTimeMs, accessCount, creationCount);
        }
    }
    
    /**
     * 🚀 EXCEPCIÓN PARA ERRORES DE CREACIÓN LAZY
     */
    public static class LazyBeanCreationException extends RuntimeException {
        public LazyBeanCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}