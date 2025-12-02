package io.warmup.examples.startup.memory;

/**
 * 🔥 HOTSPOT DE MEMORIA
 * Área de memoria con alta frecuencia de acceso identificada como crítica
 */
public class MemoryHotspot {
    private final long address;
    private final long size;
    private final MemoryAccessLevel accessLevel;
    private final long averageAccessTime;
    private final long accessCount;
    private final long detectionTime;
    private final HotspotPriority priority;
    
    public MemoryHotspot(long address, long size, MemoryAccessLevel accessLevel, 
                        long averageAccessTime, long accessCount, long detectionTime) {
        this.address = address;
        this.size = size;
        this.accessLevel = accessLevel;
        this.averageAccessTime = averageAccessTime;
        this.accessCount = accessCount;
        this.detectionTime = detectionTime;
        this.priority = calculatePriority();
    }
    
    private HotspotPriority calculatePriority() {
        if (accessCount > 100 && averageAccessTime < 1000) return HotspotPriority.CRITICAL;
        if (accessCount > 50 && averageAccessTime < 2000) return HotspotPriority.HIGH;
        if (accessCount > 20 && averageAccessTime < 5000) return HotspotPriority.MEDIUM;
        return HotspotPriority.LOW;
    }
    
    // Getters
    public long getAddress() { return address; }
    public long getSize() { return size; }
    public MemoryAccessLevel getAccessLevel() { return accessLevel; }
    public long getAverageAccessTime() { return averageAccessTime; }
    public long getAccessCount() { return accessCount; }
    public long getDetectionTime() { return detectionTime; }
    public HotspotPriority getPriority() { return priority; }
    
    @Override
    public String toString() {
        return String.format("MemoryHotspot{address=0x%X, size=%dMB, accesses=%d, time=%dns, priority=%s}", 
            address, size / 1024 / 1024, accessCount, averageAccessTime, priority);
    }
}