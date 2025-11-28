package io.warmup.framework.examples.webscopes;

import io.warmup.framework.annotation.*;
import io.warmup.framework.core.WarmupContainer;

import java.util.Map;

/**
 * Comprehensive example demonstrating the use of Web Scopes in the Warmup Framework.
 * 
 * This example shows:
 * 1. @RequestScope - beans that live for a single HTTP request
 * 2. @SessionScope - beans that live for an entire HTTP session
 * 3. @ApplicationScope - beans that live for the entire application lifecycle
 * 4. Lifecycle management and cleanup
 * 5. Integration with dependency injection
 * 6. Statistics and monitoring
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
public class WebScopesExample {
    
    // ===========================================
    // REQUEST SCOPED BEANS
    // ===========================================
    
    /**
     * Request-scoped bean for storing request-specific context.
     */
    @Component
    @RequestScope
    public static class RequestContext {
        private final String requestId;
        private final long startTime;
        
        public RequestContext() {
            this.requestId = "REQ-" + System.currentTimeMillis();
            this.startTime = System.currentTimeMillis();
        }
        
        public String getRequestId() {
            return requestId;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
        
        @Override
        public String toString() {
            return String.format("RequestContext{id=%s, elapsed=%dms}", requestId, getElapsedTime());
        }
    }
    
    /**
     * Request-scoped service that processes request-specific data.
     */
    @Component
    @RequestScope
    public static class RequestProcessor {
        private final RequestContext requestContext;
        private int requestCount = 0;
        
        @Inject
        public RequestProcessor(RequestContext requestContext) {
            this.requestContext = requestContext;
        }
        
        public void processRequest(String data) {
            requestCount++;
            System.out.println(String.format("📋 Processing request %d for %s: %s", 
                    requestCount, requestContext.getRequestId(), data));
        }

        public void handleRequest(String data) {
            processRequest(data);
        }
        
        public RequestContext getRequestContext() {
            return requestContext;
        }
        
        public int getRequestCount() {
            return requestCount;
        }
    }
    
    // ===========================================
    // SESSION SCOPED BEANS
    // ===========================================
    
    /**
     * Session-scoped bean for storing user session data.
     */
    @Component
    @SessionScope
    public static class UserSession {
        private final String sessionId;
        private final long creationTime;
        private int requestCount = 0;
        
        public UserSession() {
            this.sessionId = "SESSION-" + System.currentTimeMillis();
            this.creationTime = System.currentTimeMillis();
            System.out.println("🆕 New user session created: " + sessionId);
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public void incrementRequestCount() {
            requestCount++;
        }
        
        public int getRequestCount() {
            return requestCount;
        }
        
        @PreDestroy
        public void cleanup() {
            System.out.println("🗑️ Session cleanup: " + sessionId + " handled " + requestCount + " requests");
        }
        
        @Override
        public String toString() {
            return String.format("UserSession{id=%s, requests=%d}", sessionId, requestCount);
        }
    }
    
    /**
     * Session-scoped shopping cart for e-commerce functionality.
     */
    @Component
    @SessionScope
    public static class ShoppingCart {
        private final String sessionId;
        private final java.util.List<String> items = new java.util.ArrayList<>();
        
        @Inject
        public ShoppingCart(UserSession session) {
            this.sessionId = session.getSessionId();
        }
        
        public void addItem(String item) {
            items.add(item);
            System.out.println("🛒 Added to cart [" + sessionId + "]: " + item);
        }
        
        public void removeItem(String item) {
            items.remove(item);
            System.out.println("🗑️ Removed from cart [" + sessionId + "]: " + item);
        }
        
        public java.util.List<String> getItems() {
            return new java.util.ArrayList<>(items);
        }
        
        public int getItemCount() {
            return items.size();
        }
    }
    
    // ===========================================
    // APPLICATION SCOPED BEANS
    // ===========================================
    
    /**
     * Application-scoped configuration service.
     */
    @Component
    @ApplicationScope
    public static class ApplicationConfig {
        private final String applicationId;
        private final long startupTime;
        
        public ApplicationConfig() {
            this.applicationId = "APP-" + System.currentTimeMillis();
            this.startupTime = System.currentTimeMillis();
            System.out.println("🚀 Application initialized: " + applicationId);
        }
        
        public String getApplicationId() {
            return applicationId;
        }
        
        public long getStartupTime() {
            return startupTime;
        }
        
        public long getUptime() {
            return System.currentTimeMillis() - startupTime;
        }
        
        public String getEnvironment() {
            return "production";
        }
        
        public String getVersion() {
            return "1.2.0";
        }
        
        @Override
        public String toString() {
            return String.format("ApplicationConfig{id=%s, uptime=%dms}", applicationId, getUptime());
        }
    }
    
    /**
     * Application-scoped cache service.
     */
    @Component
    @ApplicationScope
    public static class ApplicationCache {
        private final java.util.Map<String, Object> cache = new java.util.concurrent.ConcurrentHashMap<>();
        
        public void put(String key, Object value) {
            cache.put(key, value);
        }
        
        public Object get(String key) {
            return cache.get(key);
        }
        
        public boolean containsKey(String key) {
            return cache.containsKey(key);
        }
        
        public int size() {
            return cache.size();
        }
        
        public void clear() {
            cache.clear();
        }
    }
    
    // ===========================================
    // LEGACY SINGLETON (BACKWARD COMPATIBILITY)
    // ===========================================
    
    /**
     * Traditional singleton bean (backward compatibility).
     */
    @Component
    @Singleton
    public static class LegacyService {
        private final String instanceId = "LEGACY-" + System.currentTimeMillis();
        
        public String getInstanceId() {
            return instanceId;
        }
        
        public void process(String data) {
            System.out.println("⚙️ Legacy service processing: " + data + " (instance: " + instanceId + ")");
        }
        
        @Override
        public String toString() {
            return "LegacyService{" + instanceId + "}";
        }
    }
    
    // ===========================================
    // WEB CONTROLLER (SIMULATED)
    // ===========================================
    
    /**
     * Simulated web controller that processes different types of requests.
     */
    @Component
    public static class WebController {
        private final RequestProcessor requestProcessor;
        private final UserSession userSession;
        private final ApplicationConfig appConfig;
        private final LegacyService legacyService;
        
        @Inject
        public WebController(RequestProcessor requestProcessor, 
                           UserSession userSession, 
                           ApplicationConfig appConfig,
                           LegacyService legacyService) {
            this.requestProcessor = requestProcessor;
            this.userSession = userSession;
            this.appConfig = appConfig;
            this.legacyService = legacyService;
        }
        
        public void handleRequest(String data) {
            // Update session tracking
            userSession.incrementRequestCount();
            
            // Process request
            requestProcessor.processRequest(data);
            
            // Use application config
            System.out.println(String.format("🌍 App: %s, Environment: %s, Version: %s", 
                    appConfig.getApplicationId(), 
                    appConfig.getEnvironment(), 
                    appConfig.getVersion()));
            
            // Use legacy service
            legacyService.process(data);
            
            System.out.println("📊 Session status: " + userSession);
            System.out.println("🔍 Request context: " + requestProcessor.getRequestContext());
        }
    }
    
    // ===========================================
    // DEMONSTRATION METHODS
    // ===========================================
    
    public static void demonstrateRequestScope() throws Exception {
        System.out.println("\n🔄 === REQUEST SCOPE DEMONSTRATION ===");
        
        WarmupContainer container = new WarmupContainer();
        
        container.register(RequestContext.class);
        container.register(RequestProcessor.class);
        
        // Simulate multiple requests
        String[] requests = {"User login", "View profile", "Logout", "User login", "View settings"};
        
        for (int i = 0; i < requests.length; i++) {
            String requestId = "request-" + (i + 1);
            
            // Each request gets a new RequestContext instance
            RequestProcessor processor = container.get(RequestProcessor.class);
            processor.handleRequest(requests[i]);
            
            System.out.println(String.format("🎯 Request %s processed in %dms\n", 
                    requestId, processor.getRequestContext().getElapsedTime()));
        }
        
        container.shutdown();
    }
    
    public static void demonstrateSessionScope() throws Exception {
        System.out.println("\n🗃️ === SESSION SCOPE DEMONSTRATION ===");
        
        WarmupContainer container = new WarmupContainer();
        
        container.register(UserSession.class);
        container.register(ShoppingCart.class);
        
        // Simulate user shopping sessions
        String[] sessions = {"user1", "user2", "user1", "user3", "user2"};
        String[][] userActions = {
            {"add book", "add laptop", "view cart"},
            {"add phone", "remove phone", "add tablet"},
            {"add book", "add pen", "checkout"},
            {"add monitor", "add keyboard"},
            {"add chair", "add table", "view cart"}
        };
        
        for (int i = 0; i < sessions.length; i++) {
            String sessionId = sessions[i];
            
            // Each user gets their own session context
            UserSession userSession = container.get(UserSession.class);
            ShoppingCart cart = container.get(ShoppingCart.class);
            
            System.out.println("👤 Processing actions for session: " + sessionId);
            System.out.println("📊 Session: " + userSession);
            
            // Process user actions
            for (String action : userActions[i]) {
                String[] parts = action.split(" ");
                if (parts[0].equals("add")) {
                    String item = String.join(" ", java.util.Arrays.asList(parts).subList(1, parts.length));
                    cart.addItem(item);
                } else if (parts[0].equals("remove")) {
                    String item = String.join(" ", java.util.Arrays.asList(parts).subList(1, parts.length));
                    cart.removeItem(item);
                } else if (parts[0].equals("view")) {
                    System.out.println("🛒 Cart [" + sessionId + "]: " + cart.getItemCount() + " items");
                } else if (parts[0].equals("checkout")) {
                    System.out.println("💳 Checkout [" + sessionId + "]: " + cart.getItems());
                }
            }
            
            System.out.println();
        }
        
        // Session demonstration completed
        System.out.println("📈 Session scope demonstration completed");
        
        container.shutdown();
    }
    
    public static void demonstrateApplicationScope() throws Exception {
        System.out.println("\n🚀 === APPLICATION SCOPE DEMONSTRATION ===");
        
        WarmupContainer container = new WarmupContainer();
        
        container.register(ApplicationConfig.class);
        container.register(ApplicationCache.class);
        
        // Access application-scoped beans
        ApplicationConfig config1 = container.get(ApplicationConfig.class);
        ApplicationConfig config2 = container.get(ApplicationConfig.class);
        
        System.out.println("🔧 Config instance 1: " + config1);
        System.out.println("🔧 Config instance 2: " + config2);
        System.out.println("✅ Same instance? " + (config1 == config2));
        
        // Test application cache
        ApplicationCache cache = container.get(ApplicationCache.class);
        cache.put("user_count", 42);
        cache.put("session_timeout", 1800);
        cache.put("max_connections", 100);
        
        System.out.println("💾 Cache size: " + cache.size());
        System.out.println("💾 User count: " + cache.get("user_count"));
        
        // Simulate uptime
        try {
            Thread.sleep(100); // 100ms delay
            System.out.println("⏱️ Application uptime: " + config1.getUptime() + "ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        container.shutdown();
    }
    
    public static void demonstrateScopeIntegration() throws Exception {
        System.out.println("\n🔗 === SCOPE INTEGRATION DEMONSTRATION ===");
        
        WarmupContainer container = new WarmupContainer();
        
        // Register all bean types
        container.register(RequestContext.class);
        container.register(UserSession.class);
        container.register(ApplicationConfig.class);
        container.register(ShoppingCart.class);
        container.register(LegacyService.class);
        container.register(WebController.class);
        
        // Simulate a complete user session with multiple requests
        String sessionId = "integration-session";
        String[] requests = {"Login", "Dashboard", "Add Product", "Checkout", "Logout"};
        
        // Get session-scoped beans for this user session
        
        for (int i = 0; i < requests.length; i++) {
            String requestId = "req-" + (i + 1);
            
            System.out.println("🌐 Handling request: " + requests[i]);
            
            WebController controller = container.get(WebController.class);
            controller.handleRequest(requests[i]);
            
            System.out.println();
        }
        
        // Show final statistics
        System.out.println("📊 Scope integration demonstration completed");
        
        container.shutdown();
    }
    
    public static void demonstrateBackwardCompatibility() throws Exception {
        System.out.println("\n🔄 === BACKWARD COMPATIBILITY DEMONSTRATION ===");
        
        WarmupContainer container = new WarmupContainer();
        
        // Register with traditional boolean singleton parameter (backward compatibility)
        container.register(LegacyService.class, true); // true = singleton
        
        // This should work the same as before
        LegacyService service1 = container.get(LegacyService.class);
        LegacyService service2 = container.get(LegacyService.class);
        
        System.out.println("🕰️ Legacy singleton service: " + service1.getInstanceId());
        System.out.println("✅ Same instance? " + (service1 == service2));
        
        service1.process("test data");
        
        container.shutdown();
    }
    
    // ===========================================
    // MAIN METHOD
    // ===========================================
    
    public static void main(String[] args) throws Exception {
        System.out.println("🌐 === WEB SCOPES DEMONSTRATION ===");
        System.out.println("Demonstrating @RequestScope, @SessionScope, and @ApplicationScope");
        System.out.println("in the Warmup Framework DI container");
        
        try {
            demonstrateRequestScope();
            demonstrateSessionScope();
            demonstrateApplicationScope();
            demonstrateScopeIntegration();
            demonstrateBackwardCompatibility();
            
            System.out.println("\n✅ === ALL DEMONSTRATIONS COMPLETED ===");
            System.out.println("Web Scopes functionality is working correctly!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during demonstration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}