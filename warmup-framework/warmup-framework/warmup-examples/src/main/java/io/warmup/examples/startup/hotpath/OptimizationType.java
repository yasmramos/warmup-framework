package io.warmup.examples.startup.hotpath;

/**
 * Represents different types of optimizations that can be applied to hot paths.
 * Each type represents a specific optimization strategy or technique.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum OptimizationType {
    
    /**
     * Method inlining - replacing method calls with direct code
     */
    METHOD_INLINING("Method Inlining", "Replace method calls with direct code"),
    
    /**
     * Loop unrolling - reducing loop overhead by executing multiple iterations
     */
    LOOP_UNROLLING("Loop Unrolling", "Reduce loop overhead by executing multiple iterations"),
    
    /**
     * Dead code elimination - removing unreachable or unused code
     */
    DEAD_CODE_ELIMINATION("Dead Code Elimination", "Remove unreachable or unused code"),
    
    /**
     * Constant folding - precomputing constant expressions
     */
    CONSTANT_FOLDING("Constant Folding", "Precompute constant expressions"),
    
    /**
     * Branch prediction optimization - reordering conditions
     */
    BRANCH_PREDICTION("Branch Prediction", "Optimize conditional branch predictions"),
    
    /**
     * Cache optimization - improving data locality
     */
    CACHE_OPTIMIZATION("Cache Optimization", "Improve data locality and cache performance"),
    
    /**
     * Memory layout optimization - reorganizing object memory structure
     */
    MEMORY_LAYOUT("Memory Layout", "Optimize object memory structure and alignment"),
    
    /**
     * Object pooling - reusing objects instead of creating new ones
     */
    OBJECT_POOLING("Object Pooling", "Reuse objects instead of creating new instances"),
    
    /**
     * String optimization - optimizing string operations
     */
    STRING_OPTIMIZATION("String Optimization", "Optimize string creation and manipulation"),
    
    /**
     * Generic optimization - custom or unspecified optimization
     */
    GENERIC("Generic Optimization", "Custom or unspecified optimization strategy"),
    
    /**
     * Parallel execution - running tasks in parallel to improve performance
     */
    PARALLEL_EXECUTION("Parallel Execution", "Run tasks in parallel to improve overall performance"),
    
    /**
     * Phase reordering - reorganizing startup phases for optimal performance
     */
    PHASE_REORDERING("Phase Reordering", "Reorganize startup phases for optimal performance and startup time");
    
    private final String name;
    private final String description;
    
    OptimizationType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns true if this optimization type is considered performance-focused
     */
    public boolean isPerformanceFocused() {
        return this == METHOD_INLINING || 
               this == LOOP_UNROLLING || 
               this == BRANCH_PREDICTION || 
               this == CACHE_OPTIMIZATION ||
               this == MEMORY_LAYOUT;
    }
    
    /**
     * Returns true if this optimization type is considered memory-focused
     */
    public boolean isMemoryFocused() {
        return this == DEAD_CODE_ELIMINATION ||
               this == MEMORY_LAYOUT ||
               this == OBJECT_POOLING ||
               this == STRING_OPTIMIZATION;
    }
}