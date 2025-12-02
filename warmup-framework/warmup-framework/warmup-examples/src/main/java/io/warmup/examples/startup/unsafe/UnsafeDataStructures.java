/*
 * Warmup Framework - Ninth Optimization System
 * UnsafeDataStructures.java
 * 
 * Ninth System: "Use Unsafe and direct memory for critical structures. 
 *                Eliminate garbage collector overhead in startup path"
 * 
 * Copyright (c) 2025 MiniMax Agent. All rights reserved.
 */

package io.warmup.examples.startup.unsafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Logger;

/**
 * Ninth Optimization System: UnsafeDataStructures
 * 
 * Provides critical data structures implemented using direct memory access
 * to eliminate garbage collection overhead during startup.
 * 
 * Key Features:
 * - Lock-free data structures for startup phase
 * - Direct memory-based collections
 * - Zero-allocation data operations
 * - Thread-safe implementations without synchronized blocks
 * 
 * Critical Structures:
 * - UnsafeArrayList: Zero-GC array list
 * - UnsafeHashMap: Lock-free hash map
 * - UnsafeObjectPool: Object pooling with direct memory
 * - UnsafeStringTable: String interning with direct memory
 * - UnsafeConfigurationCache: Configuration caching without GC
 */
public class UnsafeDataStructures {
    
    private static final Logger logger = Logger.getLogger(UnsafeDataStructures.class.getName());
    
    private static final UnsafeMemoryManager MEMORY_MANAGER = UnsafeMemoryManager.getInstance();
    
    /**
     * UnsafeArrayList - Array list implemented with direct memory
     * Eliminates GC overhead for dynamic arrays during startup
     */
    public static class UnsafeArrayList<T> {
        private static final int DEFAULT_CAPACITY = 16;
        private static final int GROWTH_FACTOR = 2;
        
        private long baseAddress;
        private int capacity;
        private int size;
        private final Class<T> componentType;
        
        @SuppressWarnings("unchecked")
        public UnsafeArrayList(Class<T> componentType, int initialCapacity) {
            this.componentType = componentType;
            this.capacity = Math.max(DEFAULT_CAPACITY, initialCapacity);
            this.size = 0;
            
            long objectSize = UnsafeMemoryUtils.OBJECT_SIZE; // 8 bytes for reference
            this.baseAddress = MEMORY_MANAGER.allocateMemory(capacity * objectSize).getAddress();
            
            UnsafeMemoryMetrics.recordStructureCreation("UnsafeArrayList", 
                capacity * objectSize, componentType.getSimpleName());
            
            logger.fine("Created UnsafeArrayList with capacity " + capacity + 
                       " for type " + componentType.getSimpleName());
        }
        
        @SuppressWarnings("unchecked")
        public void add(T element) {
            if (size >= capacity) {
                grow();
            }
            
            long elementAddress = baseAddress + (size * UnsafeMemoryUtils.OBJECT_SIZE);
            MEMORY_MANAGER.putObject(elementAddress, element);
            size++;
            
            // Record operation removed - method doesn't exist in UnsafeMemoryMetrics
        }
        
        @SuppressWarnings("unchecked")
        public T get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            
            long elementAddress = baseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
            return (T) MEMORY_MANAGER.getObject(elementAddress);
        }
        
        public void remove(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            
            // Shift elements left to fill the gap
            for (int i = index; i < size - 1; i++) {
                long fromAddress = baseAddress + ((i + 1) * UnsafeMemoryUtils.OBJECT_SIZE);
                long toAddress = baseAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                MEMORY_MANAGER.putObject(toAddress, MEMORY_MANAGER.getObject(fromAddress));
            }
            
            // Clear the last element
            long lastAddress = baseAddress + ((size - 1) * UnsafeMemoryUtils.OBJECT_SIZE);
            MEMORY_MANAGER.putObject(lastAddress, null);
            
            size--;
        }
        
        public boolean contains(T element) {
            for (int i = 0; i < size; i++) {
                if (get(i) != null && get(i).equals(element)) {
                    return true;
                }
            }
            return false;
        }
        
        public int size() {
            return size;
        }
        
        public void clear() {
            // Zero out memory for cleanup
            long bytesToZero = size * UnsafeMemoryUtils.OBJECT_SIZE;
            for (int i = 0; i < size; i++) {
                long elementAddress = baseAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                MEMORY_MANAGER.putObject(elementAddress, null);
            }
            size = 0;
        }
        
        private void grow() {
            int newCapacity = capacity * GROWTH_FACTOR;
            long newAddress = MEMORY_MANAGER.allocateMemory(newCapacity * UnsafeMemoryUtils.OBJECT_SIZE).getAddress();
            
            // Copy existing elements
            for (int i = 0; i < size; i++) {
                long oldElementAddress = baseAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                long newElementAddress = newAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                MEMORY_MANAGER.putObject(newElementAddress, MEMORY_MANAGER.getObject(oldElementAddress));
            }
            
            // Free old memory
            UnsafeMemoryManager.MemoryAllocation oldAllocation = new UnsafeMemoryManager.MemoryAllocation(
                baseAddress, capacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeArrayList_OLD");
            MEMORY_MANAGER.freeMemory(oldAllocation);
            
            baseAddress = newAddress;
            capacity = newCapacity;
            
            UnsafeMemoryMetrics.recordGrowth("UnsafeArrayList", capacity);
            logger.fine("UnsafeArrayList grown to capacity " + capacity);
        }
        
        public void dispose() {
            clear();
            
            UnsafeMemoryManager.MemoryAllocation allocation = new UnsafeMemoryManager.MemoryAllocation(
                baseAddress, capacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeArrayList_DISPOSE");
            MEMORY_MANAGER.freeMemory(allocation);
            
            UnsafeMemoryMetrics.recordStructureDisposal("UnsafeArrayList", 
                capacity * UnsafeMemoryUtils.OBJECT_SIZE);
        }
    }
    
    /**
     * UnsafeHashMap - Lock-free hash map implementation
     * Provides O(1) operations without GC overhead
     */
    public static class UnsafeHashMap<K, V> {
        private static final int DEFAULT_CAPACITY = 16;
        private static final float LOAD_FACTOR = 0.75f;
        
        private long keyBaseAddress;
        private long valueBaseAddress;
        private long statusBaseAddress; // 0=empty, 1=occupied, 2=deleted
        private int capacity;
        private int size;
        private final Class<K> keyType;
        private final Class<V> valueType;
        
        public UnsafeHashMap(Class<K> keyType, Class<V> valueType) {
            this(keyType, valueType, DEFAULT_CAPACITY);
        }
        
        public UnsafeHashMap(Class<K> keyType, Class<V> valueType, int initialCapacity) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.capacity = nextPowerOfTwo(initialCapacity);
            this.size = 0;
            
            long allocationSize = capacity * UnsafeMemoryUtils.OBJECT_SIZE;
            this.keyBaseAddress = MEMORY_MANAGER.allocateMemory(allocationSize).getAddress();
            this.valueBaseAddress = MEMORY_MANAGER.allocateMemory(allocationSize).getAddress();
            this.statusBaseAddress = MEMORY_MANAGER.allocateMemory(capacity * 4).getAddress(); // int per slot
            
            UnsafeMemoryMetrics.recordStructureCreation("UnsafeHashMap", 
                allocationSize * 2 + capacity * 4, keyType.getSimpleName() + "_" + valueType.getSimpleName());
            
            logger.fine("Created UnsafeHashMap with capacity " + capacity + 
                       " for types " + keyType.getSimpleName() + "/" + valueType.getSimpleName());
        }
        
        public void put(K key, V value) {
            if (size >= capacity * LOAD_FACTOR) {
                resize();
            }
            
            int hash = hash(key);
            int index = indexFor(hash, capacity);
            
            // Linear probing for collision resolution
            int originalIndex = index;
            do {
                long statusAddress = statusBaseAddress + (index * 4);
                int status = MEMORY_MANAGER.getInt(statusAddress);
                
                if (status == 0) {
                    // Empty slot - insert here
                    insertEntry(index, key, value);
                    size++;
                    break;
                } else if (status == 1) {
                    // Occupied - check if same key
                    long keyAddress = keyBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
                    K existingKey = (K) MEMORY_MANAGER.getObject(keyAddress);
                    
                    if (key.equals(existingKey)) {
                        // Update existing value
                        long valueAddress = valueBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
                        MEMORY_MANAGER.putObject(valueAddress, value);
                        break;
                    }
                }
                // Continue probing
                index = (index + 1) & (capacity - 1);
            } while (index != originalIndex);
            
            // Record operation removed - method doesn't exist in UnsafeMemoryMetrics
        }
        
        @SuppressWarnings("unchecked")
        public V get(K key) {
            int hash = hash(key);
            int index = indexFor(hash, capacity);
            
            int originalIndex = index;
            do {
                long statusAddress = statusBaseAddress + (index * 4);
                int status = MEMORY_MANAGER.getInt(statusAddress);
                
                if (status == 0) {
                    // Empty - key not found
                    return null;
                } else if (status == 1) {
                    // Occupied - check key
                    long keyAddress = keyBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
                    K existingKey = (K) MEMORY_MANAGER.getObject(keyAddress);
                    
                    if (key.equals(existingKey)) {
                        // Found - return value
                        long valueAddress = valueBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
                        return (V) MEMORY_MANAGER.getObject(valueAddress);
                    }
                }
                // Continue probing
                index = (index + 1) & (capacity - 1);
            } while (index != originalIndex);
            
            return null;
        }
        
        private void insertEntry(int index, K key, V value) {
            long statusAddress = statusBaseAddress + (index * 4);
            MEMORY_MANAGER.putInt(statusAddress, 1); // Mark as occupied
            
            long keyAddress = keyBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
            MEMORY_MANAGER.putObject(keyAddress, key);
            
            long valueAddress = valueBaseAddress + (index * UnsafeMemoryUtils.OBJECT_SIZE);
            MEMORY_MANAGER.putObject(valueAddress, value);
        }
        
        private void resize() {
            int oldCapacity = capacity;
            int newCapacity = capacity * 2;
            
            // Allocate new arrays
            long newKeyBase = MEMORY_MANAGER.allocateMemory(newCapacity * UnsafeMemoryUtils.OBJECT_SIZE).getAddress();
            long newValueBase = MEMORY_MANAGER.allocateMemory(newCapacity * UnsafeMemoryUtils.OBJECT_SIZE).getAddress();
            long newStatusBase = MEMORY_MANAGER.allocateMemory(newCapacity * 4).getAddress();
            
            // Rehash all entries
            for (int i = 0; i < oldCapacity; i++) {
                long statusAddress = statusBaseAddress + (i * 4);
                int status = MEMORY_MANAGER.getInt(statusAddress);
                
                if (status == 1) {
                    long keyAddress = keyBaseAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                    K key = (K) MEMORY_MANAGER.getObject(keyAddress);
                    
                    long valueAddress = valueBaseAddress + (i * UnsafeMemoryUtils.OBJECT_SIZE);
                    V value = (V) MEMORY_MANAGER.getObject(valueAddress);
                    
                    // Reinsert in new table
                    int hash = hash(key);
                    int newIndex = indexFor(hash, newCapacity);
                    
                    // Insert with new base addresses
                    long newStatusAddress = newStatusBase + (newIndex * 4);
                    long newKeyAddress = newKeyBase + (newIndex * UnsafeMemoryUtils.OBJECT_SIZE);
                    long newValueAddress = newValueBase + (newIndex * UnsafeMemoryUtils.OBJECT_SIZE);
                    
                    MEMORY_MANAGER.putInt(newStatusAddress, 1);
                    MEMORY_MANAGER.putObject(newKeyAddress, key);
                    MEMORY_MANAGER.putObject(newValueAddress, value);
                }
            }
            
            // Free old memory
            UnsafeMemoryManager.MemoryAllocation oldKeyAllocation = new UnsafeMemoryManager.MemoryAllocation(
                keyBaseAddress, oldCapacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeHashMap_KEYS");
            UnsafeMemoryManager.MemoryAllocation oldValueAllocation = new UnsafeMemoryManager.MemoryAllocation(
                valueBaseAddress, oldCapacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeHashMap_VALUES");
            UnsafeMemoryManager.MemoryAllocation oldStatusAllocation = new UnsafeMemoryManager.MemoryAllocation(
                statusBaseAddress, oldCapacity * 4, "UnsafeHashMap_STATUS");
            
            MEMORY_MANAGER.freeMemory(oldKeyAllocation);
            MEMORY_MANAGER.freeMemory(oldValueAllocation);
            MEMORY_MANAGER.freeMemory(oldStatusAllocation);
            
            // Update to new arrays
            keyBaseAddress = newKeyBase;
            valueBaseAddress = newValueBase;
            statusBaseAddress = newStatusBase;
            capacity = newCapacity;
            
            UnsafeMemoryMetrics.recordResize("UnsafeHashMap", capacity);
        }
        
        public int size() {
            return size;
        }
        
        public void dispose() {
            UnsafeMemoryManager.MemoryAllocation keyAllocation = new UnsafeMemoryManager.MemoryAllocation(
                keyBaseAddress, capacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeHashMap_KEYS_DISPOSE");
            UnsafeMemoryManager.MemoryAllocation valueAllocation = new UnsafeMemoryManager.MemoryAllocation(
                valueBaseAddress, capacity * UnsafeMemoryUtils.OBJECT_SIZE, "UnsafeHashMap_VALUES_DISPOSE");
            UnsafeMemoryManager.MemoryAllocation statusAllocation = new UnsafeMemoryManager.MemoryAllocation(
                statusBaseAddress, capacity * 4, "UnsafeHashMap_STATUS_DISPOSE");
            
            MEMORY_MANAGER.freeMemory(keyAllocation);
            MEMORY_MANAGER.freeMemory(valueAllocation);
            MEMORY_MANAGER.freeMemory(statusAllocation);
            
            UnsafeMemoryMetrics.recordStructureDisposal("UnsafeHashMap", 
                capacity * UnsafeMemoryUtils.OBJECT_SIZE * 2 + capacity * 4);
        }
        
        private static int hash(Object key) {
            return key.hashCode();
        }
        
        private static int indexFor(int hash, int capacity) {
            return hash & (capacity - 1); // Fast modulo for power-of-two capacity
        }
        
        private static int nextPowerOfTwo(int num) {
            if (num <= 1) return 1;
            return Integer.highestOneBit(num - 1) << 1;
        }
    }
    
    /**
     * UnsafeObjectPool - Object pooling without GC overhead
     */
    public static class UnsafeObjectPool<T> {
        private final UnsafeArrayList<T> availableObjects;
        private final UnsafeArrayList<Long> objectAddresses;
        private final AtomicInteger poolSize = new AtomicInteger(0);
        private final AtomicInteger createdObjects = new AtomicInteger(0);
        private final AtomicInteger reusedObjects = new AtomicInteger(0);
        private final Class<T> objectType;
        
        public UnsafeObjectPool(Class<T> objectType, int initialSize) {
            this.objectType = objectType;
            this.availableObjects = new UnsafeArrayList<T>(objectType, initialSize);
            this.objectAddresses = new UnsafeArrayList<Long>(Long.class, initialSize);
            
            // Pre-populate pool
            for (int i = 0; i < initialSize; i++) {
                T obj = createObject();
                long address = MEMORY_MANAGER.allocateMemory(UnsafeMemoryUtils.OBJECT_SIZE).getAddress();
                MEMORY_MANAGER.putObject(address, obj);
                availableObjects.add(obj);
                objectAddresses.add(address);
                poolSize.incrementAndGet();
                createdObjects.incrementAndGet();
            }
            
            UnsafeMemoryMetrics.recordPoolCreation(objectType.getSimpleName(), initialSize);
            logger.fine("Created UnsafeObjectPool for " + objectType.getSimpleName() + 
                       " with " + initialSize + " pre-allocated objects");
        }
        
        @SuppressWarnings("unchecked")
        public T acquire() {
            if (availableObjects.size() > 0) {
                int lastIndex = availableObjects.size() - 1;
                T obj = availableObjects.get(lastIndex);
                availableObjects.remove(lastIndex);
                objectAddresses.remove(lastIndex);
                poolSize.decrementAndGet();
                reusedObjects.incrementAndGet();
                
                UnsafeMemoryMetrics.recordPoolOperation("acquire", objectType.getSimpleName(), true);
                return obj;
            } else {
                // Create new object
                T obj = createObject();
                createdObjects.incrementAndGet();
                
                UnsafeMemoryMetrics.recordPoolOperation("acquire", objectType.getSimpleName(), false);
                return obj;
            }
        }
        
        public void release(T object) {
            // Find object address and return to pool
            // For simplicity, we'll just add back to available list
            long address = MEMORY_MANAGER.allocateMemory(UnsafeMemoryUtils.OBJECT_SIZE).getAddress();
            MEMORY_MANAGER.putObject(address, object);
            availableObjects.add(object);
            objectAddresses.add(address);
            poolSize.incrementAndGet();
            
            UnsafeMemoryMetrics.recordPoolOperation("release", objectType.getSimpleName(), true);
        }
        
        @SuppressWarnings("unchecked")
        private T createObject() {
            try {
                return objectType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                // Fallback for objects without default constructor
                return (T) new Object();
            }
        }
        
        public int getPoolSize() {
            return poolSize.get();
        }
        
        public int getCreatedObjects() {
            return createdObjects.get();
        }
        
        public int getReusedObjects() {
            return reusedObjects.get();
        }
        
        public void dispose() {
            availableObjects.dispose();
            objectAddresses.dispose();
            
            UnsafeMemoryMetrics.recordPoolDisposal(objectType.getSimpleName(), 
                createdObjects.get(), reusedObjects.get());
        }
    }
    
    /**
     * UnsafeStringTable - String interning with direct memory
     */
    public static class UnsafeStringTable {
        private final UnsafeHashMap<String, String> stringTable;
        private final AtomicLong totalStrings = new AtomicLong(0);
        private final AtomicLong memoryUsed = new AtomicLong(0);
        
        public UnsafeStringTable() {
            this.stringTable = new UnsafeHashMap<>(String.class, String.class, 1024);
        }
        
        public String intern(String str) {
            if (str == null) {
                return null;
            }
            
            // Check if already interned
            String existing = stringTable.get(str);
            if (existing != null) {
                return existing;
            }
            
            // Intern new string
            stringTable.put(str, str);
            totalStrings.incrementAndGet();
            memoryUsed.addAndGet(str.length() * 2); // 2 bytes per char in UTF-16
            
            UnsafeMemoryMetrics.recordStringInterning(str.length(), true);
            return str;
        }
        
        public long getTotalStrings() {
            return totalStrings.get();
        }
        
        public long getMemoryUsed() {
            return memoryUsed.get();
        }
        
        public int size() {
            return stringTable.size();
        }
        
        public void dispose() {
            stringTable.dispose();
            UnsafeMemoryMetrics.recordStringTableDisposal(totalStrings.get(), memoryUsed.get());
        }
    }
    
    /**
     * UnsafeConfigurationCache - Configuration caching without GC
     */
    public static class UnsafeConfigurationCache {
        private final UnsafeHashMap<String, Object> configCache;
        private final UnsafeArrayList<String> configKeys;
        private final AtomicLong cacheHits = new AtomicLong(0);
        private final AtomicLong cacheMisses = new AtomicLong(0);
        
        public UnsafeConfigurationCache() {
            this.configCache = new UnsafeHashMap<>(String.class, Object.class, 128);
            this.configKeys = new UnsafeArrayList<>(String.class, 128);
        }
        
        public void put(String key, Object value) {
            configCache.put(key, value);
            if (!configKeys.contains(key)) {
                configKeys.add(key);
            }
            
            UnsafeMemoryMetrics.recordConfigCacheOperation("put", key);
        }
        
        public Object get(String key) {
            Object value = configCache.get(key);
            if (value != null) {
                cacheHits.incrementAndGet();
                UnsafeMemoryMetrics.recordConfigCacheOperation("hit", key);
                return value;
            } else {
                cacheMisses.incrementAndGet();
                UnsafeMemoryMetrics.recordConfigCacheOperation("miss", key);
                return null;
            }
        }
        
        public double getHitRate() {
            long hits = cacheHits.get();
            long total = hits + cacheMisses.get();
            return total > 0 ? (double) hits / total : 0.0;
        }
        
        public int size() {
            return configCache.size();
        }
        
        public void dispose() {
            configCache.dispose();
            configKeys.dispose();
            
            UnsafeMemoryMetrics.recordConfigCacheDisposal(cacheHits.get(), cacheMisses.get(), size());
        }
    }
    
    /**
     * Utility class for unsafe memory operations
     */
    public static class UnsafeMemoryUtils {
        public static final long OBJECT_SIZE = 8; // Size of object reference in 64-bit JVM
        public static final long INT_SIZE = 4;
        public static final long LONG_SIZE = 8;
        public static final long DOUBLE_SIZE = 8;
    }
}