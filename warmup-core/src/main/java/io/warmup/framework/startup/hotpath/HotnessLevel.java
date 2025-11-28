package io.warmup.framework.startup.hotpath;

/**
 * Represents the level of "hotness" (frequency and performance impact) of a code path.
 * Higher levels indicate more frequently executed or performance-critical code paths.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum HotnessLevel {
    
    /**
     * Cold path - rarely executed, minimal performance impact
     */
    COLD(0, "Cold Path"),
    
    /**
     * Lukewarm path - occasionally executed, low performance impact
     */
    LUKEWARM(1, "Lukewarm Path"),
    
    /**
     * Warm path - regularly executed, moderate performance impact
     */
    WARM(2, "Warm Path"),
    
    /**
     * Hot path - frequently executed, significant performance impact
     */
    HOT(3, "Hot Path"),
    
    /**
     * Critical path - extremely frequently executed, critical performance impact
     */
    CRITICAL(4, "Critical Path"),
    
    /**
     * Very hot path - extremely frequently executed, very high performance impact
     */
    VERY_HOT(5, "Very Hot Path"),
    
    /**
     * Extremely hot path - constantly executed, maximum performance impact
     */
    EXTREMELY_HOT(6, "Extremely Hot Path");
    
    private final int level;
    private final String description;
    
    HotnessLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns true if this level represents a hot or critical path
     */
    public boolean isHot() {
        return this == HOT || this == CRITICAL || this == VERY_HOT || this == EXTREMELY_HOT;
    }
    
    /**
     * Returns true if this level represents a critical path
     */
    public boolean isCritical() {
        return this == CRITICAL || this == VERY_HOT || this == EXTREMELY_HOT;
    }
}