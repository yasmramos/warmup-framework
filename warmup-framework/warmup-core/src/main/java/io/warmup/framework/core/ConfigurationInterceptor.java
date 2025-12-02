package io.warmup.framework.core;

import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Interceptor for @Configuration classes that handles @Bean method calls.
 * This interceptor ensures that @Bean methods return singleton instances
 * and handles the lifecycle of configuration-created beans.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ConfigurationInterceptor {
    
    private static final Logger logger = Logger.getLogger(ConfigurationInterceptor.class.getName());
    
    private final Map<String, Object> beanNameToInstance;
    private final Map<Class<?>, Object> beanTypeToInstance;
    private final DependencyRegistry dependencyRegistry;
    private final WarmupContainer container;
    
    public ConfigurationInterceptor(
            Map<String, Object> beanNameToInstance,
            Map<Class<?>, Object> beanTypeToInstance,
            DependencyRegistry dependencyRegistry,
            WarmupContainer container) {
        this.beanNameToInstance = beanNameToInstance;
        this.beanTypeToInstance = beanTypeToInstance;
        this.dependencyRegistry = dependencyRegistry;
        this.container = container;
    }
    
    /**
     * Intercept @Bean method calls to ensure singleton behavior.
     */
    public Object intercept(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // Extract bean information
            String beanName = AsmCoreUtils.getName(method);
            Class<?> beanType = AsmCoreUtils.getReturnType(method);
            
            // Check if bean instance already exists
            Object existingInstance = beanTypeToInstance.get(beanType);
            if (existingInstance != null) {
                logger.fine("Returning existing bean instance for: " + beanName);
                return existingInstance;
            }
            
            // Get dependency definition for this bean
            Dependency dependency = dependencyRegistry.findDependency(beanType, beanName);
            if (dependency == null) {
                throw new IllegalStateException(
                    "No dependency definition found for bean: " + beanName + 
                    " of type: " + AsmCoreUtils.getClassName(beanType));
            }
            
            // Create new instance
            logger.fine("Creating new bean instance for: " + beanName);
            Object newInstance = dependency.getInstance(container, new java.util.HashSet<>());
            
            // Cache the instance
            beanTypeToInstance.put(beanType, newInstance);
            
            return newInstance;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to intercept @Bean method call: " + AsmCoreUtils.getName(method), e);
        }
    }
}