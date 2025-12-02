# Solución: AOP Automático para Beans Registrados Manualmente

## 📋 Problema Identificado

Los beans registrados manualmente a través de `warmup.registerBean()` no se decoraban automáticamente con AOP (Aspect-Oriented Programming), causando que las anotaciones como `@Async` no funcionaran en tests como `AsyncIntegrationTest`.

**Flujo del problema:**
1. Test registra bean manualmente: `warmup.registerBean("asyncTestService", AsyncTestService.class, serviceInstance)`
2. Bean se almacena en DependencyRegistry sin decoración AOP
3. Methods con `@Async` no se interceptan
4. Tests fallan porque los métodos se ejecutan sincrónamente

## 🔧 Solución Implementada

### 1. Modificación en DependencyRegistry.registerBeanWithScope()

**Archivo:** `/workspace/warmup-framework/warmup-core/src/main/java/io/warmup/framework/core/DependencyRegistry.java`

```java
// ✅ CRITICAL FIX: Aplicar AOP automáticamente a la instancia antes del registro
Object finalInstance = applyAopIfNeeded(instance, type);

// Usar finalInstance en lugar de instance
Dependency dependency = new Dependency(type, shouldBeSingleton, finalInstance);
```

### 2. Nuevo Método applyAopIfNeeded()

**Archivo:** `/workspace/warmup-framework/warmup-core/src/main/java/io/warmup/framework/core/DependencyRegistry.java`

```java
/**
 * ✅ NUEVO: Método para aplicar AOP a instancias durante el registro manual
 */
@SuppressWarnings("unchecked")
private <T> T applyAopIfNeeded(T instance, Class<T> type) {
    if (instance == null) {
        return null;
    }
    
    try {
        // ✅ CRITICAL FIX: Obtener AopHandler del CoreContainer y aplicar AOP
        if (coreContainer != null) {
            Object aopHandlerObj = coreContainer.getAopHandler();
            if (aopHandlerObj instanceof AopHandler) {
                AopHandler aopHandler = (AopHandler) aopHandlerObj;
                T decoratedInstance = (T) aopHandler.applyAopIfNeeded(instance, type);
                if (decoratedInstance != instance) {
                    log.log(Level.INFO, "✅ AOP aplicado automáticamente al bean registrado manualmente: {0}", type.getSimpleName());
                    return decoratedInstance;
                }
            }
        }
        return instance;
    } catch (Exception e) {
        // Log the error but don't fail the registration
        log.log(Level.WARNING, "⚠️ Failed to apply AOP to manually registered bean {0}: {1}", 
                new Object[]{type.getSimpleName(), e.getMessage()});
        return instance; // Return original instance if AOP fails
    }
}
```

### 3. Simplificación del Test

**Archivo:** `/workspace/warmup-framework/warmup-core/src/test/java/io/warmup/framework/aop/AsyncIntegrationTest.java`

```java
@BeforeEach
void setUp() throws Exception {
    warmup = Warmup.create();
    warmup.scanPackages("io.warmup.framework.aop");
    warmup.getContainer().start();
    
    // ✅ AUTOMATIC AOP: Registrar bean manualmente - AOP se aplica automáticamente
    AsyncTestService serviceInstance = new AsyncTestService();
    warmup.registerBean("asyncTestService", AsyncTestService.class, serviceInstance);
    
    // Obtener el bean - ahora debería estar decorado con AOP automáticamente
    testService = warmup.getBean(AsyncTestService.class);
}
```

### 4. Modificación Adicional en BeanRegistry

**Archivo:** `/workspace/warmup-framework/warmup-core/src/main/java/io/warmup/framework/core/BeanRegistry.java`

También se modificó BeanRegistry como respaldo, agregando:
- Campo `container` para acceso al AopHandler
- Método `setContainer()` 
- Método `applyAopIfNeeded()` propio
- Lógica de aplicación automática de AOP en `registerBean()`

## 🎯 Resultado Esperado

### Antes de la Solución:
```
Test Bean Registration: AsyncTestService (sin AOP)
Method @Async execution: SINCRÓNICA (incorrecta)
Test result: FALLA - exception no interceptada
```

### Después de la Solución:
```
Test Bean Registration: AsyncTestService (con AOP automático)
Method @Async execution: ASÍNCRONA con AsyncInterceptor
Test result: PASSA - exception correctamente interceptada
```

## 🧪 Cómo Verificar la Solución

### Compilar y Probar:
```bash
cd /workspace/warmup-framework
mvn clean compile test-compile
mvn test -Dtest=AsyncIntegrationTest
```

### Tests Específicos que Deberían Pasar:
- `testAsyncMethodExceptionPropagate` - @Async con COMPLETE_EXCEPTIONALLY
- `testAsyncMethodExceptionIgnore` - @Async con RETURN_NULL
- Todos los demás tests de AsyncIntegrationTest

## ✅ Beneficios de la Solución

1. **Transparencia**: Los usuarios no necesitan saber sobre AOP
2. **Consistencia**: Beans manuales behave igual que beans automáticos
3. **Robustez**: Fallback graceful si AOP falla
4. **Performance**: AOP se aplica una sola vez durante registro
5. **Logging**: Información clara sobre aplicación de AOP

## 🔍 Flujo Técnico

```
warmup.registerBean("name", Type.class, instance)
    ↓
WarmupContainer.registerBean(name, Type.class, instance)
    ↓
ContainerCoordinator.registerNamed(name, Type.class, instance)
    ↓
DependencyRegistry.registerBeanWithScope(name, Type.class, scope, instance)
    ↓
✅ applyAopIfNeeded(instance, Type.class)  ← NUEVA LÓGICA
    ↓
AopHandler.applyAopIfNeeded(instance, Type.class)
    ↓
AspectDecorator.createDecorator(instance, Type.class)
    ↓
Return decorated proxy instance
    ↓
Dependency(Type, singleton, decoratedInstance)
    ↓
Registered in DependencyRegistry
```

## 🚀 Impact

Esta solución resuelve el problema fundamental donde los beans registrados manualmente no recibían la misma decoración AOP que los beans automáticamente descubiertos por el framework, asegurando que toda la funcionalidad AOP (incluyendo @Async, @Cache, @Profile, etc.) funcione consistentemente.