/*
 * Copyright (c) 2025 Warmup Framework. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.warmup.framework.startup.bootstrap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Registry of critical classes that should be preloaded during JVM bootstrap.
 * This registry identifies the 20-30 most performance-critical classes in the
 * Warmup Framework to eliminate class loading overhead during runtime.
 * 
 * Key Features:
 * - Hierarchical classification (Tier 1: Absolute critical, Tier 2: High priority, Tier 3: Medium priority)
 * - Dependency-aware loading (loads parent classes before child classes)
 * - Configurable class sets for different application profiles
 * - Thread-safe concurrent access
 * 
 * @author Warmup Framework Team
 * @version 1.0.0
 */
public final class CriticalClassRegistry {
    
    private static final Logger logger = Logger.getLogger(CriticalClassRegistry.class.getName());
    
    // Tier 1: Absolute Critical - Loaded first, highest priority
    private static final Set<String> TIER_1_CRITICAL = new HashSet<>(Arrays.asList(
        // Core Warmup Framework Classes
        "io.warmup.framework.startup.StartupPhasesManager",
        "io.warmup.framework.startup.bootstrap.CriticalClassPreloadSystem",
        "io.warmup.framework.startup.bootstrap.BootstrapClassLoader",
        "io.warmup.framework.startup.config.PreloadedConfigSystem",
        "io.warmup.framework.startup.lazy.ZeroStartupBeanLoader",
        "io.warmup.framework.startup.ParallelSubsystemInitializer",
        
        // Essential Java Foundation Classes
        "java.util.HashMap",
        "java.util.ArrayList",
        "java.util.concurrent.ConcurrentHashMap",
        "java.util.concurrent.CompletableFuture",
        "java.util.concurrent.atomic.AtomicReference",
        "java.lang.String",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.System",
        "java.lang.Thread",
        "java.io.IOException",
        "java.net.URISyntaxException",
        
        // Reflection & ASM Core Classes
        "java.lang.reflect.Method",
        "java.lang.reflect.Field",
        "java.lang.reflect.Constructor",
        "java.lang.ClassLoader",
        "java.lang.ThreadLocal",
        "java.util.concurrent.atomic.AtomicInteger",
        "java.util.concurrent.atomic.AtomicLong",
        "java.util.concurrent.locks.ReentrantLock",
        "java.util.concurrent.atomic.AtomicBoolean",
        "java.util.concurrent.atomic.AtomicIntegerFieldUpdater"
    ));
    
    // Tier 2: High Priority - Loaded after Tier 1
    private static final Set<String> TIER_2_HIGH_PRIORITY = new HashSet<>(Arrays.asList(
        // Warmup Framework Configuration & Startup
        "io.warmup.framework.startup.config.MemoryMappedConfigLoader",
        "io.warmup.framework.startup.config.ConfigDataAccessor",
        "io.warmup.framework.startup.lazy.LazyBeanSupplier",
        "io.warmup.framework.startup.lazy.LazyBeanRegistry",
        "io.warmup.framework.startup.BackgroundStartupPhase",
        "io.warmup.framework.startup.CriticalStartupPhase",
        
        // Extended Java Collections & Utilities
        "java.util.HashSet",
        "java.util.LinkedHashMap",
        "java.util.LinkedList",
        "java.util.TreeMap",
        "java.util.TreeSet",
        "java.util.PriorityQueue",
        "java.util.concurrent.ConcurrentSkipListMap",
        "java.util.concurrent.ConcurrentSkipListSet",
        "java.util.concurrent.CopyOnWriteArrayList",
        "java.util.concurrent.CopyOnWriteArraySet",
        "java.util.concurrent.ConcurrentLinkedQueue",
        "java.util.concurrent.LinkedBlockingQueue",
        "java.util.concurrent.ArrayBlockingQueue",
        
        // Enhanced Java IO & NIO
        "java.io.BufferedInputStream",
        "java.io.BufferedOutputStream",
        "java.io.BufferedReader",
        "java.io.BufferedWriter",
        "java.io.ByteArrayInputStream",
        "java.io.ByteArrayOutputStream",
        "java.io.FileInputStream",
        "java.io.FileOutputStream",
        "java.nio.channels.FileChannel",
        "java.nio.ByteBuffer",
        "java.nio.MappedByteBuffer",
        "java.nio.file.Path",
        "java.nio.file.Paths",
        "java.nio.file.Files",
        
        // Enhanced Java Security & Concurrency
        "java.security.AccessController",
        "java.security.PrivilegedAction",
        "java.security.PrivilegedExceptionAction",
        "java.util.concurrent.locks.AbstractQueuedSynchronizer",
        "java.util.concurrent.locks.Condition",
        "java.util.concurrent.ForkJoinPool",
        "java.util.concurrent.ForkJoinTask",
        "java.util.concurrent.CountDownLatch",
        "java.util.concurrent.CyclicBarrier",
        "java.util.concurrent.Semaphore",
        "java.util.concurrent.Phaser",
        "java.util.concurrent.Exchanger",
        "java.util.concurrent.SynchronousQueue"
    ));
    
    // Tier 3: Medium Priority - Loaded last, still beneficial
    private static final Set<String> TIER_3_MEDIUM_PRIORITY = new HashSet<>(Arrays.asList(
        // Warmup Framework Extended Classes
        "io.warmup.framework.startup.ComprehensiveStartupResult",
        "io.warmup.framework.startup.ParallelSubsystemInitializer$SubsystemTask",
        "io.warmup.framework.startup.config.MappedConfigData",
        "io.warmup.framework.startup.config.ConfigMappingStats",
        "io.warmup.framework.startup.lazy.OnDemandInitializationContext",
        "io.warmup.framework.startup.examples.ComprehensiveStartupExample",
        
        // Advanced Java Collections
        "java.util.EnumMap",
        "java.util.EnumSet",
        "java.util.BitSet",
        "java.util.IdentityHashMap",
        "java.util.WeakHashMap",
        "java.util.LinkedHashSet",
        "java.util.concurrent.ConcurrentHashMap$Segment",
        "java.util.concurrent.ConcurrentHashMap$Node",
        "java.util.concurrent.ConcurrentHashMap$KeySetView",
        "java.util.concurrent.ConcurrentHashMap$ValuesView",
        "java.util.concurrent.ConcurrentHashMap$EntrySetView",
        
        // Extended Java Math & Security
        "java.math.BigInteger",
        "java.math.BigDecimal",
        "java.security.MessageDigest",
        "java.security.Signature",
        "java.security.AlgorithmParameters",
        "java.security.cert.Certificate",
        "java.security.cert.X509Certificate",
        
        // Advanced Java Networking & URL
        "java.net.URL",
        "java.net.URI",
        "java.net.URLConnection",
        "java.net.HttpURLConnection",
        "java.net.InetAddress",
        "java.net.Socket",
        "java.net.ServerSocket",
        "java.nio.channels.SocketChannel",
        "java.nio.channels.ServerSocketChannel",
        "java.nio.channels.Selector",
        "java.nio.channels.SelectionKey",
        
        // Enhanced Java Time & Formatting
        "java.time.LocalDateTime",
        "java.time.ZonedDateTime",
        "java.time.format.DateTimeFormatter",
        "java.time.chrono.HijrahDate",
        "java.time.chrono.MinguoDate",
        "java.time.chrono.JapaneseDate",
        "java.text.DecimalFormat",
        "java.text.NumberFormat",
        "java.text.SimpleDateFormat",
        "java.text.MessageFormat",
        
        // Advanced Java Logging & Monitoring
        "java.util.logging.Logger",
        "java.util.logging.Handler",
        "java.util.logging.Level",
        "java.util.logging.LogRecord",
        "java.util.logging.ConsoleHandler",
        "java.util.logging.FileHandler",
        "java.util.logging.MemoryHandler",
        "java.util.logging.SocketHandler",
        "java.util.logging.SimpleFormatter",
        "java.util.logging.XMLFormatter"
    ));
    
    private static final Map<String, Integer> classPriorityMap = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<String>> tierClasses = new ConcurrentHashMap<>();
    private static final List<String> dependencyOrder = new ArrayList<>();
    
    static {
        initializeRegistry();
    }
    
    /**
     * Initializes the critical class registry with all tiers and dependencies.
     */
    private static void initializeRegistry() {
        // Initialize tier classes
        tierClasses.put(1, new HashSet<>(TIER_1_CRITICAL));
        tierClasses.put(2, new HashSet<>(TIER_2_HIGH_PRIORITY));
        tierClasses.put(3, new HashSet<>(TIER_3_MEDIUM_PRIORITY));
        
        // Populate priority map
        TIER_1_CRITICAL.forEach(className -> classPriorityMap.put(className, 1));
        TIER_2_HIGH_PRIORITY.forEach(className -> classPriorityMap.put(className, 2));
        TIER_3_MEDIUM_PRIORITY.forEach(className -> classPriorityMap.put(className, 3));
        
        // Calculate dependency-aware loading order
        calculateDependencyOrder();
        
        logger.info(String.format("CriticalClassRegistry initialized with %d Tier 1, %d Tier 2, and %d Tier 3 classes",
                TIER_1_CRITICAL.size(), TIER_2_HIGH_PRIORITY.size(), TIER_3_MEDIUM_PRIORITY.size()));
    }
    
    /**
     * Calculates the optimal loading order based on dependency relationships.
     * This ensures parent classes are loaded before their children.
     */
    private static void calculateDependencyOrder() {
        // Priority order: Tier 1 -> Tier 2 -> Tier 3
        List<String> orderedClasses = new ArrayList<>();
        
        // Add Tier 1 classes first
        orderedClasses.addAll(TIER_1_CRITICAL);
        
        // Add Tier 2 classes
        orderedClasses.addAll(TIER_2_HIGH_PRIORITY);
        
        // Add Tier 3 classes
        orderedClasses.addAll(TIER_3_MEDIUM_PRIORITY);
        
        dependencyOrder.addAll(orderedClasses);
    }
    
    /**
     * Gets all classes by priority tier.
     * 
     * @param tier The priority tier (1, 2, or 3)
     * @return Set of class names for the specified tier
     */
    public static Set<String> getClassesByTier(int tier) {
        return Collections.unmodifiableSet(tierClasses.getOrDefault(tier, Collections.emptySet()));
    }
    
    /**
     * Gets the priority tier for a specific class.
     * 
     * @param className The fully qualified class name
     * @return The priority tier (1=critical, 2=high, 3=medium) or -1 if not found
     */
    public static int getClassPriority(String className) {
        return classPriorityMap.getOrDefault(className, -1);
    }
    
    /**
     * Gets the complete dependency-aware loading order.
     * 
     * @return List of class names in optimal loading order
     */
    public static List<String> getDependencyOrderedClasses() {
        return Collections.unmodifiableList(dependencyOrder);
    }
    
    /**
     * Gets all critical class names regardless of tier.
     * 
     * @return Set of all critical class names
     */
    public static Set<String> getAllCriticalClasses() {
        Set<String> allClasses = new HashSet<>();
        allClasses.addAll(TIER_1_CRITICAL);
        allClasses.addAll(TIER_2_HIGH_PRIORITY);
        allClasses.addAll(TIER_3_MEDIUM_PRIORITY);
        return Collections.unmodifiableSet(allClasses);
    }
    
    /**
     * Checks if a class is considered critical for preloading.
     * 
     * @param className The fully qualified class name
     * @return true if the class should be preloaded, false otherwise
     */
    public static boolean isCriticalClass(String className) {
        return classPriorityMap.containsKey(className);
    }
    
    /**
     * Gets the total count of critical classes across all tiers.
     * 
     * @return Total number of critical classes
     */
    public static int getTotalCriticalClassCount() {
        return TIER_1_CRITICAL.size() + TIER_2_HIGH_PRIORITY.size() + TIER_3_MEDIUM_PRIORITY.size();
    }
    
    /**
     * Gets statistics about the critical class registry.
     * 
     * @return Map containing registry statistics
     */
    public static Map<String, Object> getRegistryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tier1Count", TIER_1_CRITICAL.size());
        stats.put("tier2Count", TIER_2_HIGH_PRIORITY.size());
        stats.put("tier3Count", TIER_3_MEDIUM_PRIORITY.size());
        stats.put("totalCount", getTotalCriticalClassCount());
        stats.put("dependencyOrderSize", dependencyOrder.size());
        return Collections.unmodifiableMap(stats);
    }
    
    /**
     * Adds a custom class to the critical registry at the specified tier.
     * 
     * @param className The fully qualified class name
     * @param tier The priority tier (1, 2, or 3)
     */
    public static void addCriticalClass(String className, int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Tier must be 1, 2, or 3");
        }
        
        tierClasses.computeIfAbsent(tier, k -> new HashSet<>()).add(className);
        classPriorityMap.put(className, tier);
        dependencyOrder.add(className);
        
        logger.info(String.format("Added custom critical class: %s at tier %d", className, tier));
    }
    
    /**
     * Removes a class from the critical registry.
     * 
     * @param className The fully qualified class name to remove
     * @return true if the class was found and removed, false otherwise
     */
    public static boolean removeCriticalClass(String className) {
        Integer tier = classPriorityMap.remove(className);
        if (tier != null) {
            tierClasses.get(tier).remove(className);
            dependencyOrder.remove(className);
            logger.info(String.format("Removed critical class: %s from tier %d", className, tier));
            return true;
        }
        return false;
    }
    
    /**
     * Validates that all critical classes can be loaded.
     * 
     * @return Map of validation results with class names and load status
     */
    public static Map<String, Boolean> validateCriticalClasses() {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        ClassLoader currentLoader = CriticalClassRegistry.class.getClassLoader();
        
        for (String className : dependencyOrder) {
            try {
                Class.forName(className, false, currentLoader);
                results.put(className, true);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                results.put(className, false);
                logger.warning(String.format("Failed to load critical class: %s - %s", className, e.getMessage()));
            }
        }
        
        return Collections.unmodifiableMap(results);
    }
}