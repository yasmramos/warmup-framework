package io.warmup.examples.startup.hotpath;

/**
 * Represents the confidence level in optimization recommendations.
 * Higher confidence indicates more reliable and safe optimization opportunities.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum ConfidenceLevel {
    
    /**
     * Very low confidence - high uncertainty in optimization benefit
     */
    VERY_LOW(1, "Very Low Confidence"),
    
    /**
     * Low confidence - uncertain optimization outcome
     */
    LOW(2, "Low Confidence"),
    
    /**
     * Medium confidence - reasonable expectation of improvement
     */
    MEDIUM(3, "Medium Confidence"),
    
    /**
     * High confidence - likely to provide significant improvement
     */
    HIGH(4, "High Confidence"),
    
    /**
     * Very high confidence - very likely to provide substantial improvement
     */
    VERY_HIGH(5, "Very High Confidence");
    
    private final int level;
    private final String description;
    
    ConfidenceLevel(int level, String description) {
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
     * Returns true if this confidence level is considered actionable (MEDIUM or higher)
     */
    public boolean isActionable() {
        return this.ordinal() >= MEDIUM.ordinal();
    }
    
    /**
     * Returns true if this confidence level is considered high (HIGH or VERY_HIGH)
     */
    public boolean isHigh() {
        return this == HIGH || this == VERY_HIGH;
    }
    
    /**
     * Returns the threshold value for this confidence level
     */
    public double getThreshold() {
        return level * 0.2; // Map level 1-5 to threshold 0.2-1.0
    }
}