package io.warmup.examples.startup.hotpath;

/**
 * Represents the risk level associated with applying optimizations.
 * Higher risk indicates potential for introducing bugs or breaking existing functionality.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum RiskLevel {
    
    /**
     * Minimal risk - very safe optimization with high confidence
     */
    MINIMAL(1, "Minimal Risk", "Very safe optimization"),
    
    /**
     * Low risk - generally safe with minor considerations
     */
    LOW(2, "Low Risk", "Generally safe with minor considerations"),
    
    /**
     * Medium risk - requires careful testing and validation
     */
    MEDIUM(3, "Medium Risk", "Requires careful testing and validation"),
    
    /**
     * High risk - significant potential for issues
     */
    HIGH(4, "High Risk", "Significant potential for introducing issues"),
    
    /**
     * Critical risk - very likely to cause problems
     */
    CRITICAL(5, "Critical Risk", "Very likely to cause serious problems");
    
    private final int level;
    private final String name;
    private final String description;
    
    RiskLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns true if this risk level is considered acceptable for production
     */
    public boolean isAcceptable() {
        return this == MINIMAL || this == LOW;
    }
    
    /**
     * Returns true if this risk level requires careful consideration
     */
    public boolean requiresConsideration() {
        return this == MEDIUM;
    }
    
    /**
     * Returns true if this risk level is considered high risk
     */
    public boolean isHighRisk() {
        return this == HIGH || this == CRITICAL;
    }
}