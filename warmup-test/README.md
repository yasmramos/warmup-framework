# 🎯 Warmup Test Framework - Zero-Config Testing

## Resumen Ejecutivo

El **Warmup Test Framework** es un marco de testing zero-config que elimina completamente la necesidad de configuraciones manuales en tests. Con solo agregar `@WarmupTest` a una clase, obtienes:

- ✅ **Auto-mocking** automático de dependencias
- ✅ **Auto-inyección** de mocks en spies
- ✅ **Zero manual setup** - ningún @BeforeEach requerido
- ✅ **Detección inteligente** de dependencias
- ✅ **Múltiples modos** de testing (UNIT, INTEGRATION, SYSTEM)

## 🚀 Características Principales

### 1. Anotación @WarmupTest - Magia Automática

```java
@WarmupTest
public class MyTest {
    // El framework automáticamente:
    // 1. Escanea todos los campos @Mock y @Spy
    // 2. Crea los mocks y spies  
    // 3. Inyecta dependencias automáticamente
    // 4. Maneja el lifecycle completo
}
```

### 2. @Mock vs @Spy - Comportamiento Inteligente

```java
@WarmupTest
public class ServiceTest {
    @Mock
    private Repository repo; 
    // → Mock completo: todos los métodos retornan defaults
    // → Perfecto para dependencias externas
    
    @Spy
    private Service service;
    // → Spy sobre instancia real: métodos reales a menos que se mockeen
    // → Ideal para el sistema bajo test
    
    @Spy(realImplementation = false)  
    private Service mockedService;
    // → Spy sobre mock: punto medio entre @Mock y @Spy real
}
```

### 3. Auto-Inyección de Dependencias

```java
@WarmupTest
public class OrderServiceTest {
    @Spy
    private OrderService orderService; // Necesita PaymentService
    
    @Mock  
    private PaymentService paymentService; // Auto-inyectado en OrderService
    
    @Mock
    private NotificationService notificationService; // Auto-inyectado también
    
    // El framework detecta que OrderService tiene dependencias
    // y automáticamente inyecta los mocks correspondientes
}
```

### 4. Configuraciones Avanzadas

```java
@WarmupTest(
    mode = TestMode.INTEGRATION,  // UNIT | INTEGRATION | SYSTEM
    autoMock = true,              // Auto-mockear dependencias no declaradas
    warmupTime = "2s",            // Pre-warm antes de tests
    verbose = true                // Habilitar logging detallado
)
public class AdvancedTest {
    @Spy
    @InjectMocks  // ← Opcional - para claridad
    private ComplexService service;
    
    @Mock
    @MockConfig(verbose = true, serializable = true)
    private ExternalService external;
}
```

## 📋 Casos de Uso Completos

### Caso 1: Test Unitario Simple

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest
public class UserServiceTest {
    @Spy                    // Sistema bajo test - instancia real
    private UserService userService;
    
    @Mock                   // Dependencia - mock completo  
    private UserRepository userRepo;
    
    @Mock
    private EmailService emailService;
    
    @Test
    public void createUser_validUser_createsSuccessfully() {
        // Arrange
        User user = new User("john@example.com");
        when(userRepo.save(any())).thenReturn(user);
        
        // Act
        User result = userService.createUser(user);
        
        // Assert  
        assertThat(result).isEqualTo(user);
        verify(emailService).sendWelcomeEmail(user.getEmail());
    }
}
```

### Caso 2: Test con Dependencias Complejas

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest
public class OrderProcessingTest {
    @Spy
    private OrderValidator validator;
    
    @Spy  
    private PaymentProcessor paymentProcessor;
    
    @Spy
    private InventoryManager inventoryManager;
    
    @Spy  // ← Servicio principal que usa todos los anteriores
    private OrderService orderService;
    
    @Mock
    private ShippingService shippingService;
    
    @Test
    public void processOrder_completeFlow_success() {
        // Todos los @Spy tienen instancias reales
        // Todos los @Mock tienen mocks completos
        // Las dependencias están automáticamente inyectadas
        // Solo escribir la lógica del test, nada de setup
    }
}
```

### Caso 3: Test de Integración Parcial

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest(mode = TestMode.INTEGRATION)
public class PaymentIntegrationTest {
    @Spy(useRealDependencies = true)  // Usar implementaciones reales cuando sea posible
    private PaymentService paymentService;
    
    @Mock  // Pero mockear el gateway externo
    private BankGateway bankGateway;
    
    @RealBean  // Forzar bean real (si está en contexto)
    private TransactionRepository txRepo;
}
```

## ⚙️ Algoritmo de Detección Automática

### Paso 1: Análisis de Dependencias

```
@WarmupTest detecta:
- Campos con @Spy → candidatos a sistema bajo test  
- Campos con @Mock → dependencias a mockear
- Analiza constructores y setters para inyección
```

### Paso 2: Resolución Automática

```java
// Ejemplo: El framework detecta que:
@Spy
private UserService userService;  // Tiene constructor: UserService(UserRepository, EmailService)

// Entonces busca en la clase test:
@Mock  
private UserRepository userRepository;  // ← Match por tipo
@Mock
private EmailService emailService;      // ← Match por tipo

// Y los inyecta automáticamente
```

### Paso 3: Lifecycle Management

```java
@WarmupTest
public class Test {
    // Framework ejecuta automáticamente:
    // 1. @BeforeWarmup (si existe) - custom setup
    // 2. Create all @Mock instances
    // 3. Create all @SPY instances with real implementations  
    // 4. Inject dependencies automatically
    // 5. @AfterWarmup (si existe) - post-setup
    // 6. Run tests
    // 7. Cleanup everything
}
```

## 🛠️ Manejo de Errores Inteligente

### Dependencias Faltantes:

```java
@WarmupTest
public class ProblematicTest {
    @Spy
    private UserService userService;  // Necesita UserRepository
    
    // Missing: @Mock private UserRepository repo;
    
    // ERROR AMIGABLE:
    // "Missing dependency for UserService: UserRepository not found in test class.
    // Suggested fix: Add '@Mock private UserRepository repo;'"
}
```

### Dependencias Circulares:

```java
@WarmupTest  
public class CircularTest {
    @Spy
    private ServiceA serviceA;  // Depende de ServiceB
    
    @Spy  
    private ServiceB serviceB;  // Depende de ServiceA
    
    // El framework detecta y resuelve automáticamente
    // usando proxys o instanciación lazy
}
```

## 🏗️ Arquitectura Técnica

### Estructura de Clases

```
io.warmup.test/
├── annotation/          # Anotaciones principales
│   ├── WarmupTest.java
│   ├── Mock.java
│   ├── Spy.java
│   ├── InjectMocks.java
│   └── RealBean.java
├── config/              # Configuraciones
│   ├── TestMode.java
│   ├── MockConfig.java
│   └── SpyConfig.java
├── core/                # Lógica principal
│   ├── WarmupTestExtension.java
│   ├── MockRepository.java
│   ├── DependencyAnalyzer.java
│   ├── Injector.java
│   └── AutoConfigurer.java
├── exception/           # Excepciones
│   └── WarmupTestException.java
└── examples/           # Ejemplos de uso
    ├── UserServiceTest.java
    ├── OrderProcessingTest.java
    └── PaymentIntegrationTest.java
```

### Flujo de Ejecución

```mermaid
graph TD
    A[@WarmupTest Class] --> B[Analyze Fields]
    B --> C[Create Mock Instances]
    B --> D[Create Spy Instances]
    C --> E[Inject Dependencies]
    D --> E
    E --> F[Bind to Test Instance]
    F --> G[Run Tests]
    G --> H[Cleanup]
```

## 📊 Beneficios vs Enfoque Tradicional

| Aspecto | Tradicional | WarmupTest |
|---------|-------------|------------|
| **Configuración manual** | ✅ Required | ❌ Zero |
| **@BeforeEach setup** | ✅ Required | ❌ Eliminado |
| **@InjectMocks** | ✅ Manual | ❌ Auto |
| **Mock injection** | ✅ Manual | ❌ Auto |
| **Dependency tracking** | ❌ No | ✅ Auto |
| **Circular deps** | ❌ Manual resolution | ✅ Auto resolution |
| **Time to write test** | 10-15 min | 2-3 min |

## 🎯 Ejemplos Prácticos

### Ejemplo Básico

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest
public class CalculatorTest {
    
    @Spy
    private Calculator calculator;  // Sistema bajo test
    
    @Mock
    private Logger logger;           // Dependencia externa
    
    @Test
    public void add_twoNumbers_returnsSum() {
        // Solo lógica del test, zero setup!
        double result = calculator.add(5.0, 3.0);
        
        assertThat(result).isEqualTo(8.0);
        verify(logger).log("Calculating: 5.0 + 3.0");
    }
}
```

### Ejemplo con Configuración Avanzada

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest(
    mode = TestMode.INTEGRATION,
    autoMock = true,
    warmupTime = "1s"
)
public class UserServiceIntegrationTest {
    
    @Spy
    @SpyConfig(trackCalls = true)
    private UserService userService;
    
    @Mock
    @MockConfig(verbose = true)
    private EmailService emailService;
    
    @RealBean
    private UserRepository userRepository;  // Bean real del contexto
    
    @Test
    public void createUser_withRealRepo_callsAllServices() {
        // Test de integración que usa beans reales donde es posible
        User user = userService.createUser("john@example.com");
        
        assertThat(user).isNotNull();
        verify(emailService).sendWelcomeEmail("john@example.com");
        // userRepository.save() fue llamado automáticamente
    }
}
```

## 🚦 Getting Started

### 1. Agregar Dependencia

```xml
<dependency>
    <groupId>io.warmup</groupId>
    <artifactId>warmup-test</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Usar en Tests

```java
@ExtendWith(WarmupTestExtension.class)
@WarmupTest
public class MyTest {
    @Mock
    private MyRepository repository;
    
    @Spy
    private MyService service;  // Auto-inyectado con repository
    
    @Test
    public void testSomething() {
        // Tu lógica de test aquí
        // Todo el setup es automático!
    }
}
```

## 🎉 Conclusión

El **Warmup Test Framework** transforma la experiencia de testing eliminando completamente la configuración manual. Con `@WarmupTest`, los desarrolladores pueden:

- **Escribir tests más rápido** (2-3 min vs 10-15 min)
- **Enfocarse en la lógica** en lugar de la configuración
- **Reducir errores** por configuración incorrecta
- **Mantenimiento simplificado** de tests

¡La experiencia es tan fluida que los desarrolladores ni siquiera piensan en la configuración!