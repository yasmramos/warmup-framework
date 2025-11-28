# 🔧 Plan de Corrección de Errores de Tests

## Objetivo
Corregir todos los errores de compilación en los tests del framework Warmup para permitir su ejecución exitosa.

## Categorización de Errores

### 1. Métodos Faltantes en WarmupContainer
- `getEventBus()` - Agregar método
- `reloadClass(String)` - Agregar método  
- `disableAutoShutdown()` - Agregar método
- `shutdownNow()` - Agregar método
- `isPhasedStartupEnabled()` - Agregar método
- `isProfileActive(String)` - Agregar método
- `processConfigurations()` - Agregar método
- `getDependency(Class<T>, Set<Class<?>>)` - Corregir firma
- `getBean(String)` - Agregar overload
- `setActiveProfiles(String[])` - Agregar método

### 2. APIs Incompatibles
- `getStartupMetrics()` debe devolver `StartupMetrics` no `Map<String, Object>`
- `startBackgroundPhase()` debe devolver `CompletableFuture<Void>`
- Firmas de `register()` incorrectas

### 3. Conversiones de Tipos
- `String[]` vs `String` en profiles
- Casts incorrectos en getBean
- Incompatibilidades de tipos en assertions

## Estrategia de Corrección
1. **Fase 1**: Agregar métodos faltantes a WarmupContainer
2. **Fase 2**: Corregir firmas de métodos existentes
3. **Fase 3**: Actualizar tipos de retorno apropiados
4. **Fase 4**: Compilar y verificar correcciones

## Estado Actual
- ✅ warmup-core main code: 100% compilando
- ❌ warmup-core tests: Múltiples errores por corregir
- 📊 Objetivo: 100% tests compilando y ejecutables