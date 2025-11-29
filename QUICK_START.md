# Quick Start Guide

**Warmup Framework** is a ultra-fast, high-performance dependency injection container designed for modern Java applications. This guide will get you up and running with Warmup Framework in minutes, showcasing its lightning-fast startup times and intelligent bean management.

## 🚀 Basic Example

Create a simple application using the public API:

```java
import io.warmup.framework.core.Warmup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// 1. Define your service
@Singleton
public class GreetingService {
    @Inject
    private MessageFormatter formatter;
    
    public String greet(String name) {
        return formatter.format("Hello, %s!", name);
    }
}

// 2. Define dependencies
@Singleton
public class MessageFormatter {
    public String format(String template, Object... args) {
        return String.format(template, args);
    }
}

// 3. Bootstrap your application
public class QuickStartApplication {
    public static void main(String[] args) {
        // Use the public API - Warmup class as entry point
        Warmup warmup = Warmup.create()
            .scanPackages("com.example") // Auto-discover @Singleton beans
            .start();
        
        // Get your service
        GreetingService service = warmup.getBean(GreetingService.class);
        
        // Use it
        String greeting = service.greet("World");
        System.out.println(greeting); // Output: Hello, World!
        
        // Clean shutdown
        warmup.stop();
    }
}
```

## ⚡ Even Quicker Start

For rapid prototyping, use the quick start:

```java
public class MinimalExample {
    public static void main(String[] args) {
        Warmup warmup = Warmup.quickStart(); // Auto-configured container
        
        // Your beans are automatically discovered and injected
        MyService service = warmup.getBean(MyService.class);
        service.doWork();
        
        warmup.stop();
    }
}
```

## 🔧 Configuration Options

### With Profiles
```java
Warmup warmup = Warmup.create()
    .withProfile("production") // Activate @Profile("production") beans
    .start();
```

### With Properties
```java
Warmup warmup = Warmup.create()
    .withProperty("db.url", "jdbc:h2:mem:test")
    .withProperty("cache.enabled", "true")
    .start();
```

### Multiple Profiles
```java
Warmup warmup = Warmup.create()
    .withProfiles("development", "test") // Multiple active profiles
    .start();
```

### Package Scanning
```java
Warmup warmup = Warmup.create()
    .scanPackages(
        "com.company.service",
        "com.company.controller",
        "com.company.repository"
    )
    .start();
```

## 📦 Using @Primary and @Alternative

```java
// Primary implementation - will be chosen by default
@Primary
@Singleton
public class ConsoleLogger implements Logger {
    public void log(String message) {
        System.out.println("[CONSOLE] " + message);
    }
}

// Alternative implementation - only used when explicitly requested
@Alternative
@Singleton
public class FileLogger implements Logger {
    public void log(String message) {
        // Write to file
    }
}

// Application choosing between implementations
public class LoggerExample {
    public static void main(String[] args) {
        Warmup warmup = Warmup.create()
            .withProfile("production") // Will use @Primary in production
            .start();
            
        // Gets the @Primary implementation
        Logger logger = warmup.getBean(Logger.class);
        logger.log("Application started");
    }
}
```

## 🎯 Advanced Configuration

### Command Line Arguments
```java
// Parse command line args automatically
public class ConfigurableApplication {
    public static void main(String[] args) {
        Warmup warmup = Warmup.run(args) // Parse --profile=dev, --db.url=...
            .withProperty("app.name", "MyApp")
            .start();
    }
}
```

### Environment-Specific Startup
```java
// Development
Warmup dev = Warmup.devMode();

// Testing  
Warmup test = Warmup.testMode();

// Production
Warmup prod = Warmup.prodMode();
```

### Async Startup
```java
public class AsyncApplication {
    public static void main(String[] args) {
        Warmup.create()
            .scanPackages("com.example")
            .startAsync() // Non-blocking startup
            .thenAccept(warmup -> {
                // Container is ready
                MyService service = warmup.getBean(MyService.class);
                service.start();
            });
    }
}
```

## 🧪 Testing with Warmup

```java
// JUnit 5 integration
@ExtendWith(WarmupTestExtension.class)
class MyServiceTest {
    @Inject
    private MyService service;
    
    @Test
    void shouldProcessCorrectly() {
        // Service is automatically injected and ready
        assertThat(service.process("input")).isEqualTo("expected");
    }
}

// Manual test setup
@Test
void testWithTestMode() {
    Warmup warmup = Warmup.testMode();
    
    try {
        MyService service = warmup.getBean(MyService.class);
        assertThat(service).isNotNull();
    } finally {
        warmup.stop();
    }
}
```

## 📊 Monitoring and Metrics

```java
@Singleton
public class HealthChecker {
    @Inject
    private Warmup warmup; // Inject the warmup instance
    
    public void checkHealth() {
        // Access metrics
        var metrics = warmup.getMetrics();
        System.out.println("Active beans: " + warmup.getBeanCount());
        System.out.println("Container state: " + warmup.getState());
        
        // Check if running
        if (warmup.isRunning()) {
            System.out.println("✅ Container is healthy");
        }
    }
}
```

## 🔌 Integration Examples

### Spring Boot
```java
@SpringBootApplication
public class SpringWarmupApplication {
    public static void main(String[] args) {
        // Start Warmup alongside Spring
        Warmup warmup = Warmup.quickStart();
        SpringApplication.run(SpringWarmupApplication.class, args);
    }
}
```

### Standalone JAR
```bash
# Build
mvn clean package

# Run
java -jar target/warmup-examples-1.0-SNAPSHOT.jar

# With profile
java -jar target/warmup-examples-1.0-SNAPSHOT.jar --spring.profiles.active=production
```

## 🎯 Best Practices

1. **Always call `warmup.stop()`** in a try-finally block or use try-with-resources
2. **Use profiles** to configure different environments (dev, test, prod)
3. **Scan specific packages** rather than using auto-scan for better performance
4. **Use `@Primary`** for default implementations, `@Alternative` for specific cases
5. **Inject interfaces**, not concrete classes, for better flexibility

## 🔧 Troubleshooting

### Container won't start
```java
// Check if container is already running
if (warmup.isRunning()) {
    warmup.restart(); // Restart instead of creating new
}
```

### Bean not found
```java
// Verify bean discovery
System.out.println("Active beans: " + warmup.getBeanCount());
System.out.println("Available beans: " + warmup.getInfo());
```

### Performance issues
```java
// Enable detailed metrics
Warmup warmup = Warmup.create()
    .scanPackages("your.packages")
    .start();

// Check performance metrics
var metrics = warmup.getMetrics();
System.out.println("Startup time: " + metrics.getStartupTime());
```

## 📚 Next Steps

- Read the full [README.md](README.md) for comprehensive documentation
- Check out [examples](warmup-examples/) for real-world use cases
- Run [benchmarks](warmup-benchmark/) to see performance comparisons
- Read the [Contributing Guide](CONTRIBUTING.md) to contribute to the project

---

**Need help?** Check our [GitHub Issues](https://github.com/your-org/warmup-framework/issues) or start a [Discussion](https://github.com/your-org/warmup-framework/discussions).