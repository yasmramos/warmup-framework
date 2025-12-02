package io.warmup.framework.core.optimized;

import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.metrics.MetricsManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

/**
 * 🚀 JIT ENGINE - High-performance bytecode generation and optimization
 * 
 * This engine provides:
 * - O(1) supplier caching and retrieval
 * - Bytecode generation with ASM
 * - Progressive instance creation strategies
 * - Memory-efficient class loading
 * - Performance metrics tracking
 * 
 * Architecture:
 * - ASM-based bytecode generation
 * - Cached supplier pattern
 * - Progressive construction strategies
 * - Memory-safe class loading
 * 
 * @author Warmup Framework
 * @version 2.0
 */
public class JITEngine {
    
    private static final Logger log = Logger.getLogger(JITEngine.class.getName());
    
    // ✅ SUPPLIER REGISTRY - O(1) access
    private final Map<Class<?>, Supplier<?>> jitSuppliers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> jitFactoryMethods = new ConcurrentHashMap<>();
    
    // ✅ PERFORMANCE METRICS
    private final AtomicLong suppliersGenerated = new AtomicLong(0);
    private final AtomicLong classesDefined = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // ✅ DEPENDENCIES
    private final ASMCacheManager cacheManager;
    private final MetricsManager metricsManager;
    
    public JITEngine(ASMCacheManager cacheManager, MetricsManager metricsManager) {
        this.cacheManager = cacheManager;
        this.metricsManager = metricsManager;
        
        log.info("🚀 JITEngine initialized with ASM optimization");
    }
    
    /**
     * 🚀 Create JIT-optimized instance with progressive strategy
     */
    @SuppressWarnings("unchecked")
    public <T> T createInstanceJit(Class<T> clazz) {
        long startTime = System.nanoTime();
        boolean success = false;
        
        try {
            // ✅ FAST PATH: Check JIT cache first
            Supplier<?> cachedSupplier = jitSuppliers.get(clazz);
            if (cachedSupplier != null) {
                cacheHits.incrementAndGet();
                T result = (T) cachedSupplier.get();
                success = true;
                return result;
            }
            
            cacheMisses.incrementAndGet();
            
            // ✅ SLOW PATH: Generate and cache supplier
            Supplier<T> supplier = generateJitSupplier(clazz);
            jitSuppliers.put(clazz, supplier);
            suppliersGenerated.incrementAndGet();
            
            T result = supplier.get();
            success = true;
            return result;
            
        } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, 
                   "JIT creation failed for " + clazz.getSimpleName() + ", using reflection fallback", e);
            return createWithReflectionFallback(clazz);
        } finally {
            long duration = System.nanoTime() - startTime;
            metricsManager.getContainerMetrics().recordRequest(success);
            
            if (success) {
                log.log(java.util.logging.Level.FINEST, 
                       "JIT instance created: {0} in {1} ns", 
                       new Object[]{clazz.getSimpleName(), duration});
            }
        }
    }
    
    /**
     * 🎯 Create instance (alias for createInstanceJit)
     */
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> clazz) throws Exception {
        return createInstanceJit(clazz);
    }
    
    /**
     * 🚀 Generate JIT supplier with ASM bytecode generation
     */
    private <T> Supplier<T> generateJitSupplier(Class<T> clazz) {
        try {
            String className = clazz.getName().replace('.', '/');
            String supplierClassName = className + "_JITSupplier";
            String cacheKey = supplierClassName;
            
            // ✅ Check bytecode cache first
            byte[] originalClassData = getClassBytecode(clazz);
            String sourceHash = cacheManager.calculateSourceHash(originalClassData);
            byte[] cachedBytecode = cacheManager.getCachedBytecode(cacheKey, sourceHash);
            
            if (cachedBytecode != null) {
                log.log(java.util.logging.Level.FINE, "Cache hit for JIT supplier: {0}", clazz.getName());
                Class<?> cachedClass = defineClass(supplierClassName.replace('/', '.'), cachedBytecode);
                return (Supplier<T>) AsmCoreUtils.newInstanceProgressiveWithClass(cachedClass);
            }
            
            log.log(java.util.logging.Level.FINE, "Generating new JIT supplier for: {0}", clazz.getName());
            
            // ✅ Generate bytecode using ASM
            byte[] bytecode = generateSupplierBytecode(clazz, supplierClassName);
            
            // ✅ Cache the generated bytecode
            cacheManager.cacheBytecode(cacheKey, sourceHash, bytecode);
            
            // ✅ Load and instantiate the supplier class
            Class<?> supplierClass = defineClass(supplierClassName.replace('/', '.'), bytecode);
            return (Supplier<T>) AsmCoreUtils.newInstanceProgressiveWithClass(supplierClass);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JIT supplier for " + clazz.getName(), e);
        }
    }
    
    /**
     * 🚀 Generate supplier bytecode using ASM
     */
    private <T> byte[] generateSupplierBytecode(Class<T> clazz, String supplierClassName) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        
        // Define supplier class
        cw.visit(V1_8, ACC_PUBLIC, supplierClassName,
                "Ljava/lang/Object;Ljava/util/function/Supplier;",
                "java/lang/Object", new String[]{"java/util/function/Supplier"});
        
        // Generate constructor
        generateConstructor(cw);
        
        // Generate get() method
        generateGetMethod(cw, clazz.getName().replace('.', '/'), clazz);
        
        cw.visitEnd();
        classesDefined.incrementAndGet();
        
        return cw.toByteArray();
    }
    
    /**
     * 🚀 Generate constructor bytecode
     */
    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    /**
     * 🚀 Generate get() method bytecode
     */
    private <T> void generateGetMethod(ClassWriter cw, String targetClassName, Class<T> clazz) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        
        try {
            Constructor<?> constructor = findInjectableConstructor(clazz);
            Class<?>[] paramTypes = constructor.getParameterTypes();
            
            // NEW instruction - create new instance
            mv.visitTypeInsn(NEW, targetClassName);
            mv.visitInsn(DUP);
            
            // Load constructor parameters (default values for now)
            for (Class<?> paramType : paramTypes) {
                loadDefaultValue(mv, paramType);
            }
            
            // INVOKESPECIAL constructor call
            String constructorDesc = Type.getConstructorDescriptor(constructor);
            mv.visitMethodInsn(INVOKESPECIAL, targetClassName, "<init>", constructorDesc, false);
            
            // RETURN
            mv.visitInsn(ARETURN);
            
        } catch (Exception e) {
            // Fallback: return null if constructor can't be found
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        }
        
        mv.visitMaxs(4, 1);
        mv.visitEnd();
    }
    
    /**
     * 🚀 Find injectable constructor (simplified)
     */
    private Constructor<?> findInjectableConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        // Find constructor with @Inject annotation or single constructor
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(javax.inject.Inject.class)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        
        // Default: use no-args constructor if available
        try {
            Constructor<?> noArgs = clazz.getDeclaredConstructor();
            noArgs.setAccessible(true);
            return noArgs;
        } catch (NoSuchMethodException e) {
            // Last resort: use first constructor
            if (constructors.length > 0) {
                constructors[0].setAccessible(true);
                return constructors[0];
            }
            throw e;
        }
    }
    
    /**
     * 🚀 Load default value for primitive types
     */
    private void loadDefaultValue(MethodVisitor mv, Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            mv.visitInsn(ICONST_0); // false
        } else if (type == byte.class || type == Byte.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == char.class || type == Character.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == short.class || type == Short.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == int.class || type == Integer.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == long.class || type == Long.class) {
            mv.visitInsn(LCONST_0);
        } else if (type == float.class || type == Float.class) {
            mv.visitInsn(FCONST_0);
        } else if (type == double.class || type == Double.class) {
            mv.visitInsn(DCONST_0);
        } else {
            mv.visitInsn(ACONST_NULL); // null for objects
        }
    }
    
    /**
     * 🚀 Get class bytecode for hash calculation
     */
    private byte[] getClassBytecode(Class<?> clazz) {
        try {
            String classPath = clazz.getName().replace('.', '/') + ".class";
            java.io.InputStream is = clazz.getClassLoader().getResourceAsStream(classPath);
            
            if (is == null) {
                // Fallback: use class name bytes
                return clazz.getName().getBytes();
            }
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            return clazz.getName().getBytes();
        }
    }
    
    /**
     * 🚀 Define class using ASM classloader
     */
    private Class<?> defineClass(String name, byte[] bytecode) throws Exception {
        return new ASMClassLoader().defineClass(name, bytecode);
    }
    
    /**
     * 🚀 Fallback to reflection for non-JIT compatible classes
     */
    private <T> T createWithReflectionFallback(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance with reflection fallback", e);
        }
    }
    
    /**
     * 🚀 Precompile components for JIT optimization
     */
    public void precompileComponents(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            precompileComponent(clazz);
        }
    }
    
    /**
     * 🚀 Precompile single component
     */
    public void precompileComponent(Class<?> clazz) {
        try {
            Supplier<?> supplier = generateJitSupplier(clazz);
            jitSuppliers.put(clazz, supplier);
            log.log(java.util.logging.Level.INFO, "✅ Component pre-compiled JIT: {0}", clazz.getSimpleName());
        } catch (Exception e) {
            log.log(java.util.logging.Level.WARNING, 
                   "⚠️ Pre-compilation JIT failed for {0}: {1}",
                   new Object[]{clazz.getSimpleName(), e.getMessage()});
        }
    }
    
    /**
     * 🚀 Get JIT statistics
     */
    public Map<String, Object> getJITStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("suppliersGenerated", suppliersGenerated.get());
        stats.put("classesDefined", classesDefined.get());
        stats.put("cacheHits", cacheHits.get());
        stats.put("cacheMisses", cacheMisses.get());
        stats.put("cacheHitRate", calculateCacheHitRate());
        stats.put("activeSuppliers", jitSuppliers.size());
        
        // List precompiled components
        List<String> precompiled = jitSuppliers.keySet().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        stats.put("precompiledComponents", precompiled);
        
        return stats;
    }
    
    /**
     * 🚀 Calculate cache hit rate
     */
    private double calculateCacheHitRate() {
        long hits = cacheHits.get();
        long total = hits + cacheMisses.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) hits / total * 100.0;
    }
    
    /**
     * 🚀 Clear JIT cache
     */
    public void clearCache() {
        jitSuppliers.clear();
        jitFactoryMethods.clear();
        
        // Reset metrics
        suppliersGenerated.set(0);
        classesDefined.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        
        log.info("🧹 JIT cache cleared");
    }
    
    /**
     * 🚀 Check if class is JIT optimized
     */
    public boolean isJitOptimized(Class<?> clazz) {
        return jitSuppliers.containsKey(clazz);
    }
    
    /**
     * 🚀 Get optimized component count
     */
    public int getOptimizedComponentCount() {
        return jitSuppliers.size();
    }
    
    /**
     * 🚀 ASM ClassLoader for safe class loading
     */
    private static class ASMClassLoader extends ClassLoader {
        public ASMClassLoader() {
            super(ASMClassLoader.class.getClassLoader());
        }
        
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}