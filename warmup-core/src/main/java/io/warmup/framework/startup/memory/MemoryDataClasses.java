package io.warmup.framework.startup.memory;

import java.util.Objects;

/**
 * 🗂️ CLASES DE DATOS Y ENUMERACIONES PARA OPTIMIZACIÓN DE MEMORIA
 * 
 * Contiene todas las clases de datos, enumeraciones y estructuras utilizadas
 * por el sistema de optimización de memoria.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */

// ===== CLASES DE DATOS =====

/**
 * 🗂️ REGIÓN DE MEMORIA
 * Representa una región específica de memoria identificada durante el análisis
 */
class MemoryRegion {
    private final String name;
    private final long address;
    private final long size;
    private final MemoryRegionType type;
    private final MemoryAccessLevel accessLevel;
    private final long detectionTime;
    private double fragmentationRatio = 0.0;
    
    public MemoryRegion(String name, long address, long size, MemoryRegionType type, 
                       MemoryAccessLevel accessLevel, long detectionTime) {
        this.name = name;
        this.address = address;
        this.size = size;
        this.type = type;
        this.accessLevel = accessLevel;
        this.detectionTime = detectionTime;
    }
    
    // Getters
    public String getName() { return name; }
    public long getAddress() { return address; }
    public long getSize() { return size; }
    public MemoryRegionType getType() { return type; }
    public MemoryAccessLevel getAccessLevel() { return accessLevel; }
    public long getDetectionTime() { return detectionTime; }
    public double getFragmentationRatio() { return fragmentationRatio; }
    
    // Setters
    public void setFragmentationRatio(double fragmentationRatio) {
        this.fragmentationRatio = fragmentationRatio;
    }
    
    @Override
    public String toString() {
        return String.format("MemoryRegion{name='%s', address=0x%X, size=%dMB, type=%s, access=%s}", 
            name, address, size / 1024 / 1024, type, accessLevel);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryRegion that = (MemoryRegion) o;
        return Objects.equals(name, that.name) && 
               Objects.equals(address, that.address) &&
               Objects.equals(size, that.size);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, address, size);
    }
}
/**
 * 📊 PATRÓN DE ACCESO A PÁGINA
 * Datos sobre cómo se accede a una página específica de memoria
 */
class PageAccessPattern {
    private final long address;
    private final int size;
    private final PageTemperature temperature;
    private final long averageAccessTime;
    private final long accessCount;
    private final long lastAccessTime;
    private final AccessPatternType patternType;
    
    public PageAccessPattern(long address, int size, PageTemperature temperature, 
                           long averageAccessTime, long accessCount, long lastAccessTime) {
        this.address = address;
        this.size = size;
        this.temperature = temperature;
        this.averageAccessTime = averageAccessTime;
        this.accessCount = accessCount;
        this.lastAccessTime = lastAccessTime;
        this.patternType = determinePatternType();
    }
    
    private AccessPatternType determinePatternType() {
        if (accessCount == 1) return AccessPatternType.ONE_TIME;
        if (accessCount <= 5) return AccessPatternType.SPARSE;
        if (accessCount <= 20) return AccessPatternType.MODERATE;
        return AccessPatternType.FREQUENT;
    }
    
    // Getters
    public long getAddress() { return address; }
    public int getSize() { return size; }
    public PageTemperature getTemperature() { return temperature; }
    public long getAverageAccessTime() { return averageAccessTime; }
    public long getAccessCount() { return accessCount; }
    public long getLastAccessTime() { return lastAccessTime; }
    public AccessPatternType getPatternType() { return patternType; }
    
    @Override
    public String toString() {
        return String.format("PageAccessPattern{address=0x%X, temp=%s, accesses=%d, time=%dns}", 
            address, temperature, accessCount, averageAccessTime);
    }
}

// ===== ENUMERACIONES =====

/**
 * 🏷️ TIPO DE REGIÓN DE MEMORIA
 */
enum MemoryRegionType {
    HEAP("Heap principal"),
    STACK("Stack de threads"),
    CODE_CACHE("Cache de código JIT"),
    METADATA("Metadatos de clases"),
    PRELOAD_CANDIDATE("Candidato para pre-carga"),
    BUFFER("Buffer de datos"),
    MAPPED_FILE("Archivo mapeado en memoria"),
    SHARED_MEMORY("Memoria compartida"),
    SYSTEM_RESERVED("Reservado por el sistema");
    
    private final String description;
    
    MemoryRegionType(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 📊 NIVEL DE ACCESO A MEMORIA
 */
enum MemoryAccessLevel {
    CRITICAL("Crítico - Acceso esencial para funcionalidad básica"),
    HIGH("Alto - Acceso frecuente en operaciones importantes"),
    MEDIUM("Medio - Acceso moderado en operaciones normales"),
    LOW("Bajo - Acceso ocasional o de mantenimiento"),
    MINIMAL("Mínimo - Acceso muy raro o de debugging");
    
    private final String description;
    
    MemoryAccessLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 🌡️ TEMPERATURA DE PÁGINA
 * Indica qué tan frecuentemente se accede a una página
 */
enum PageTemperature {
    EXTREMELY_HOT("Extremadamente caliente - Acceso muy frecuente"),
    VERY_HOT("Muy caliente - Acceso frecuente"),
    HOT("Caliente - Acceso regular"),
    WARM("Tibia - Acceso ocasional"),
    COLD("Fría - Acceso muy raro"),
    FROZEN("Congelada - Sin accesos o muy espaciados");
    
    private final String description;
    
    PageTemperature(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 🎯 PRIORIDAD DE PRE-FETCH
 * Nivel de prioridad para pre-carga de páginas
 */
enum PrefetchPriority {
    CRITICAL("Crítico - Pre-cargar inmediatamente"),
    HIGH("Alto - Pre-cargar en fase temprana"),
    MEDIUM("Medio - Pre-cargar en fase media"),
    LOW("Bajo - Pre-cargar si hay recursos disponibles"),
    DEFERRED("Diferido - Pre-cargar en background");
    
    private final String description;
    
    PrefetchPriority(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 🛠️ ESTRATEGIA DE PRE-FETCH
 * Método específico para pre-cargar memoria
 */
enum PrefetchStrategy {
    AGGRESSIVE("Agresivo - Acceder a cada byte"),
    STANDARD("Estándar - Acceder a páginas estratégicas"),
    FORCED_FAULT("Page Fault Forzado - Forzar page faults deliberadamente"),
    ADAPTIVE("Adaptativo - Ajustar según patrones"),
    BACKGROUND("Background - Pre-carga no bloqueante");
    
    private final String description;
    
    PrefetchStrategy(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 🚨 PRIORIDAD DE HOTSPOT
 * Nivel de criticidad de un hotspot de memoria
 */
enum HotspotPriority {
    CRITICAL("Crítico - Requiere optimización inmediata"),
    HIGH("Alto - Importante para performance"),
    MEDIUM("Medio - Beneficioso optimizar"),
    LOW("Bajo - Optimización opcional");
    
    private final String description;
    
    HotspotPriority(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

/**
 * 📈 TIPO DE PATRÓN DE ACCESO
 * Clasifica el patrón de acceso a memoria
 */
enum AccessPatternType {
    ONE_TIME("Acceso único"),
    SPARSE("Acceso disperso (1-5 veces)"),
    MODERATE("Acceso moderado (6-20 veces)"),
    FREQUENT("Acceso frecuente (21+ veces)"),
    BURST("Acceso en ráfagas"),
    PERIODIC("Acceso periódico"),
    RANDOM("Acceso aleatorio");
    
    private final String description;
    
    AccessPatternType(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}

// ===== CLASES DE EXCEPCIÓN =====

/**
 * ❌ EXCEPCIÓN DE OPTIMIZACIÓN DE MEMORIA
 * Excepción específica para errores en optimización de memoria
 */
class MemoryOptimizationException extends RuntimeException {
    private final String errorCode;
    private final Object[] parameters;
    
    public MemoryOptimizationException(String message) {
        super(message);
        this.errorCode = "MEM_OPT_GENERIC";
        this.parameters = new Object[0];
    }
    
    public MemoryOptimizationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MEM_OPT_GENERIC";
        this.parameters = new Object[0];
    }
    
    public MemoryOptimizationException(String errorCode, String message, Object... parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }
    
    public MemoryOptimizationException(String errorCode, String message, Throwable cause, Object... parameters) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }
    
    public String getErrorCode() { return errorCode; }
    public Object[] getParameters() { return parameters.clone(); }
    
    @Override
    public String toString() {
        return String.format("MemoryOptimizationException[%s]: %s", errorCode, getMessage());
    }
}