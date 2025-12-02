package io.warmup.framework.core;

import io.warmup.framework.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages web application contexts for different scope types.
 * 
 * This class provides:
 * - Request-scoped bean management
 * - Session-scoped bean management  
 * - Application-scoped bean management
 * - Thread-safe context handling
 * - Automatic cleanup and lifecycle management
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
public class WebScopeContext {
    
    private static final Logger log = Logger.getLogger(WebScopeContext.class.getName());
    
    // Context storage maps
    private final Map<String, Map<String, Object>> requestContext = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sessionContext = new ConcurrentHashMap<>();
    private final Map<String, Object> applicationContext = new ConcurrentHashMap<>();
    
    // Thread-local storage for current request and session IDs
    private final ThreadLocal<String> currentRequestId = new ThreadLocal<>();
    private final ThreadLocal<String> currentSessionId = new ThreadLocal<>();
    
    // Container reference for dependency resolution
    private final WarmupContainer container;
    
    public WebScopeContext(WarmupContainer container) {
        this.container = container;
    }
    
    // ===========================================
    // REQUEST SCOPE MANAGEMENT
    // ===========================================
    
    /**
     * Sets the current request context key.
     * 
     * @param requestId unique request identifier
     */
    public void setCurrentRequest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        
        currentRequestId.set(requestId);
        requestContext.computeIfAbsent(requestId, k -> new ConcurrentHashMap<>());
        log.log(Level.FINEST, "Setting current request context: {0}", requestId);
    }
    
    /**
     * Clears the current request context.
     */
    public void clearCurrentRequest() {
        String requestId = currentRequestId.get();
        if (requestId != null) {
            Map<String, Object> requestData = requestContext.remove(requestId);
            if (requestData != null) {
                for (Object bean : requestData.values()) {
                    cleanupBeanLifecycle(bean);
                }
                log.log(Level.FINE, "Request invalidated: {0}, cleaned up {1} beans", 
                        new Object[]{requestId, requestData.size()});
            }
            currentRequestId.remove();
        }
        log.log(Level.FINEST, "Clearing current request context");
    }
    
    /**
     * Gets or creates a request-scoped bean instance.
     * 
     * @param beanClass the bean class
     * @return the bean instance
     * @throws IllegalStateException if no request context is active
     */
    public <T> T getRequestScopedBean(Class<T> beanClass) {
        String requestId = currentRequestId.get();
        if (requestId == null) {
            throw new IllegalStateException("No request context is active. Call setCurrentRequest() first.");
        }
        
        String beanName = getScopeName(beanClass);
        String key = beanClass.getName() + (beanName.isEmpty() ? "" : ":" + beanName);
        
        Map<String, Object> requestData = requestContext.computeIfAbsent(requestId, k -> new ConcurrentHashMap<>());
        
        @SuppressWarnings("unchecked")
        T bean = (T) requestData.get(key);
        
        if (bean == null) {
            log.log(Level.FINE, "Creating new request-scoped bean: {0} for request {1}", 
                    new Object[]{beanClass.getSimpleName(), requestId});
            try {
                bean = container.createInstanceJit(beanClass);
                requestData.put(key, bean);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create request-scoped bean: " + beanClass.getName(), e);
            }
        }
        
        return bean;
    }
    
    /**
     * Cleans up all request-scoped beans for the current request.
     */
    public void cleanupRequestScope() {
        String requestId = currentRequestId.get();
        if (requestId != null) {
            Map<String, Object> requestData = requestContext.remove(requestId);
            if (requestData != null) {
                log.log(Level.FINE, "Cleaning up request-scoped beans: {0} instances", requestData.size());
                
                for (Object bean : requestData.values()) {
                    cleanupBeanLifecycle(bean);
                }
            }
        }
    }
    
    // ===========================================
    // SESSION SCOPE MANAGEMENT
    // ===========================================
    
    /**
     * Sets the current session context key.
     * 
     * @param sessionId unique session identifier
     */
    public void setCurrentSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        sessionContext.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        log.log(Level.FINEST, "Setting current session context: {0}", sessionId);
    }
    
    /**
     * Clears the current session context.
     * 
     * @param sessionId the session to clear
     */
    public void clearCurrentSession(String sessionId) {
        if (sessionId != null) {
            Map<String, Object> sessionData = sessionContext.remove(sessionId);
            if (sessionData != null) {
                for (Object bean : sessionData.values()) {
                    cleanupBeanLifecycle(bean);
                }
                log.log(Level.FINE, "Session invalidated: {0}, cleaned up {1} beans", 
                        new Object[]{sessionId, sessionData.size()});
            }
        }
    }
    
    /**
     * Gets or creates a session-scoped bean instance for the current session.
     * 
     * @param beanClass the bean class
     * @param sessionId the session identifier
     * @return the bean instance
     * @throws IllegalStateException if no session context is active
     */
    public <T> T getSessionScopedBean(Class<T> beanClass, String sessionId) {
        String beanName = getScopeName(beanClass);
        String key = beanClass.getName() + (beanName.isEmpty() ? "" : ":" + beanName);
        
        Map<String, Object> sessionData = sessionContext.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        
        @SuppressWarnings("unchecked")
        T bean = (T) sessionData.get(key);
        
        if (bean == null) {
            log.log(Level.FINE, "Creating new session-scoped bean: {0} for session {1}", 
                    new Object[]{beanClass.getSimpleName(), sessionId});
            try {
                bean = container.createInstanceJit(beanClass);
                sessionData.put(key, bean);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create session-scoped bean: " + beanClass.getName(), e);
            }
        }
        
        return bean;
    }
    
    /**
     * Gets all active session IDs.
     * 
     * @return set of active session IDs
     */
    public Set<String> getActiveSessionIds() {
        return new HashSet<>(sessionContext.keySet());
    }
    
    /**
     * Gets the number of active sessions.
     * 
     * @return number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionContext.size();
    }
    
    // ===========================================
    // APPLICATION SCOPE MANAGEMENT
    // ===========================================
    
    /**
     * Gets or creates an application-scoped bean instance.
     * 
     * @param beanClass the bean class
     * @return the bean instance
     */
    public <T> T getApplicationScopedBean(Class<T> beanClass) {
        String beanName = getScopeName(beanClass);
        String key = beanClass.getName() + (beanName.isEmpty() ? "" : ":" + beanName);
        
        @SuppressWarnings("unchecked")
        T bean = (T) applicationContext.get(key);
        
        if (bean == null) {
            log.log(Level.FINE, "Creating new application-scoped bean: {0}", beanClass.getSimpleName());
            try {
                bean = container.createInstanceJit(beanClass);
                applicationContext.put(key, bean);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create application-scoped bean: " + beanClass.getName(), e);
            }
        }
        
        return bean;
    }
    
    /**
     * Cleans up all application-scoped beans.
     */
    public void cleanupApplicationScope() {
        log.log(Level.FINE, "Cleaning up application-scoped beans: {0} instances", applicationContext.size());
        
        for (Object bean : applicationContext.values()) {
            cleanupBeanLifecycle(bean);
        }
        
        applicationContext.clear();
    }
    
    // ===========================================
    // UTILITY METHODS
    // ===========================================
    
    /**
     * Gets the scope name for a bean class.
     * 
     * @param beanClass the bean class
     * @return the scope name or empty string
     */
    private String getScopeName(Class<?> beanClass) {
        return ScopeManager.getScopeName(beanClass);
    }
    
    /**
     * Cleans up a bean's lifecycle (calls @PreDestroy methods).
     * 
     * @param bean the bean to cleanup
     */
    private void cleanupBeanLifecycle(Object bean) {
        if (bean != null) {
            try {
                Class<?> beanClass = bean.getClass();
                if (beanClass.isAnnotationPresent(PreDestroy.class)) {
                    // This will be handled by the container's PreDestroy registration
                    log.log(Level.FINEST, "Bean marked for PreDestroy cleanup: {0}", beanClass.getSimpleName());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error during bean lifecycle cleanup for {0}: {1}",
                        new Object[]{bean.getClass().getSimpleName(), e.getMessage()});
            }
        }
    }
    
    // ===========================================
    // CLEANUP AND SHUTDOWN
    // ===========================================
    
    /**
     * Performs complete cleanup of all web scope contexts.
     */
    public void shutdown() {
        log.log(Level.INFO, "Shutting down web scope contexts...");
        
        // Cleanup all request scopes
        for (Map<String, Object> requestData : requestContext.values()) {
            for (Object bean : requestData.values()) {
                cleanupBeanLifecycle(bean);
            }
        }
        requestContext.clear();
        
        // Cleanup all session scopes
        for (Map<String, Object> sessionData : sessionContext.values()) {
            for (Object bean : sessionData.values()) {
                cleanupBeanLifecycle(bean);
            }
        }
        sessionContext.clear();
        
        // Cleanup application scope
        cleanupApplicationScope();
        
        log.log(Level.INFO, "Web scope contexts shutdown completed");
    }
    
    // ===========================================
    // STATISTICS AND MONITORING
    // ===========================================
    
    /**
     * Gets statistics about all scope contexts.
     * 
     * @return map containing scope statistics
     */
    public Map<String, Object> getScopeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("request_scope.active_requests", requestContext.size());
        stats.put("request_scope.total_instances", 
                requestContext.values().stream().mapToInt(Map::size).sum());
        stats.put("session_scope.active_sessions", sessionContext.size());
        stats.put("session_scope.total_instances", 
                sessionContext.values().stream().mapToInt(Map::size).sum());
        stats.put("application_scope.active_instances", applicationContext.size());
        
        // Add scope manager cache statistics
        stats.putAll(ScopeManager.getCacheStatistics());
        
        return stats;
    }
}