# 🧪 INSTRUCCIONES PARA EJECUTAR TESTS CRÍTICOS

## 📋 TESTS IMPLEMENTADOS

**Archivo**: `warmup-framework/warmup-core/src/test/java/io/warmup/framework/core/test/critical/WarmupContainerCriticalTests.java`

**Total de tests**: 18 tests críticos organizados en 7 categorías

---

## 🚀 COMANDOS PARA EJECUTAR

### 1. Compilar el proyecto
```bash
cd warmup-framework
mvn clean compile
```

### 2. Ejecutar solo los tests críticos
```bash
cd warmup-framework
mvn test -Dtest=WarmupContainerCriticalTests
```

### 3. Ejecutar todos los tests del módulo core
```bash
cd warmup-framework
mvn test
```

### 4. Ejecutar con reporte de cobertura
```bash
cd warmup-framework
mvn test jacoco:report
```

---

## 📊 VER RESULTADOS

### Reporte de cobertura (HTML)
```bash
open warmup-core/target/site/jacoco/index.html
```

### Resultados de tests (texto)
```bash
cat warmup-core/target/surefire-reports/*.txt
```

---

## 🔧 REQUISITOS DEL ENTORNO

### Java
- **Java 8+** (el proyecto está configurado para Java 8)
- **Maven 3.6+**

### Dependencias incluidas en pom.xml:
- JUnit 5 (jupiter)
- Mockito (para mocking)
- JaCoCo (para cobertura)

---

## 🎯 TESTS INCLUIDOS

### 🔴 Prioridad Crítica:
1. **Constructores alternativos** (4 tests)
2. **Error handling post-shutdown** (3 tests)
3. **Validación de estado** (2 tests)
4. **Edge cases críticos** (5 tests)
5. **Gestión de perfiles** (2 tests)
6. **Métricas y estadísticas** (1 test)
7. **Integración con Warmup** (1 test)

### 📝 Estructura de cada test:
- `@DisplayName` descriptivo
- Logging detallado con `java.util.logging.Logger`
- Limpieza automática en `@AfterEach`
- Manejo robusto de excepciones

---

## 🐛 TROUBLESHOOTING

### Si falla la compilación:
```bash
mvn clean install -U
```

### Si faltan dependencias:
```bash
mvn dependency:resolve
```

### Si quieres ejecutar un test específico:
```bash
mvn test -Dtest=WarmupContainerCriticalTests#testBeanRetrievalAfterShutdown
```

---

## 📈 INTERPRETAR RESULTADOS

### ✅ Test exitoso:
```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

### ❌ Test fallido:
```
[INFO] Tests run: 17, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Tests run: 1, Failures: 0, Errors: 1
```

### Cobertura esperada:
- **Antes**: ~45% para WarmupContainer
- **Después**: ~65% con estos tests críticos

---

## 🔄 PRÓXIMOS PASOS

1. **Ejecutar tests** y verificar que pasen
2. **Revisar cobertura** de código
3. **Implementar tests de prioridad alta** (lifecycle management)
4. **Expandir a otros componentes** del framework

**¡Los tests están listos para ejecutarse y mejorar la cobertura del framework!**