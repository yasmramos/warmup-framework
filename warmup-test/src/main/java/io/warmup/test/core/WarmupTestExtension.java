package io.warmup.test.core;

import io.warmup.test.annotation.*;
import io.warmup.test.config.TestMode;
import io.warmup.test.exception.WarmupTestException;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.internal.configuration.ClassInfo;
import org.mockito.internal.configuration.DefaultInjectionEngine;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.creation.instance.ObjectInstantiator;
import org.mockito.internal.creation.instance.MockitoInstantiator;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.mockito.internal.util.reflection.FieldInitializationReport;
import org.mockito.internal.util.reflection.FieldInitializer;
import org.mockito.internal.util.reflection.GenericMaster;
import org.mockito.internal.util.reflection.LenientCopyTool;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.listeners.VerificationStartedListener;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension JUnit 5 que implementa el comportamiento zero-config del framework.
 * 
 * Esta clase detecta automáticamente:
 * - Clases con @WarmupTest
 * - Campos con @Mock y @Spy
 * - Dependencias entre objetos
 * - Inyección automática de mocks en spies
 */
public class WarmupTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    
    private static final ThreadLocal<WarmupTestContext> currentContext = new ThreadLocal<>();
    private static final Map<Class<?>, TestClassMetadata> classMetadataCache = new ConcurrentHashMap<>();
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        WarmupTest warmupTest = testClass.getAnnotation(WarmupTest.class);
        
        if (warmupTest == null) {
            return; // No es una clase de test con @WarmupTest
        }
        
        WarmupTestContext testContext = new WarmupTestContext(testClass, warmupTest);
        testContext.initialize();
        
        currentContext.set(testContext);
        
        // Ejecutar warm-up si está configurado
        if (!"0s".equals(warmupTest.warmupTime()) && !"0ms".equals(warmupTest.warmupTime())) {
            performWarmup(warmupTest.warmupTime());
        }
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        WarmupTestContext testContext = currentContext.get();
        if (testContext != null) {
            testContext.setupTestInstance(context.getRequiredTestInstance());
        }
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        // Cleanup del test actual
        WarmupTestContext testContext = currentContext.get();
        if (testContext != null) {
            testContext.cleanup();
        }
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Limpiar completamente
        currentContext.remove();
    }
    
    private void performWarmup(String warmupTime) {
        try {
            long millis = parseTime(warmupTime);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private long parseTime(String timeStr) {
        timeStr = timeStr.toLowerCase().trim();
        
        if (timeStr.endsWith("ms")) {
            return Long.parseLong(timeStr.substring(0, timeStr.length() - 2));
        } else if (timeStr.endsWith("s")) {
            return Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 1000;
        } else {
            return Long.parseLong(timeStr);
        }
    }
    
    /**
     * Contexto interno que mantiene el estado de cada clase de test.
     */
    private static class WarmupTestContext {
        private final Class<?> testClass;
        private final WarmupTest warmupTest;
        private final MockRepository mockRepository = new MockRepository();
        private final DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();
        private final Injector injector = new Injector();
        private final Map<Field, Object> fieldInstances = new ConcurrentHashMap<>();
        
        public WarmupTestContext(Class<?> testClass, WarmupTest warmupTest) {
            this.testClass = testClass;
            this.warmupTest = warmupTest;
        }
        
        public void initialize() throws Exception {
            // Configurar Mockito
            setupMockito();
            
            // Analizar metadata de la clase de test
            TestClassMetadata metadata = analyzeTestClass();
            
            // Crear instancias según los modos
            createInstances(metadata);
            
            // Realizar inyección de dependencias
            performDependencyInjection(metadata);
        }
        
        public void setupTestInstance(Object testInstance) {
            // Mapear instancias a los campos del test
            bindInstancesToTest(testInstance);
        }
        
        public void cleanup() {
            mockRepository.clearAll();
            fieldInstances.clear();
        }
        
        private void setupMockito() {
            // Configuración global de Mockito
            Mockito.reset();
        }
        
        private TestClassMetadata analyzeTestClass() {
            return classMetadataCache.computeIfAbsent(testClass, cls -> {
                TestClassMetadata metadata = new TestClassMetadata();
                
                for (Field field : cls.getDeclaredFields()) {
                    Mock mockAnnotation = field.getAnnotation(Mock.class);
                    Spy spyAnnotation = field.getAnnotation(Spy.class);
                    RealBean realBeanAnnotation = field.getAnnotation(RealBean.class);
                    
                    if (mockAnnotation != null) {
                        metadata.addMockField(field, mockAnnotation);
                    } else if (spyAnnotation != null) {
                        metadata.addSpyField(field, spyAnnotation);
                    } else if (realBeanAnnotation != null) {
                        metadata.addRealBeanField(field, realBeanAnnotation);
                    }
                }
                
                // Analizar dependencias entre mocks y spies
                dependencyAnalyzer.analyzeDependencies(metadata);
                
                return metadata;
            });
        }
        
        private void createInstances(TestClassMetadata metadata) throws Exception {
            // Crear mocks primero
            for (MockField mockField : metadata.getMockFields()) {
                Object mockInstance = createMock(mockField);
                fieldInstances.put(mockField.getField(), mockInstance);
            }
            
            // Crear spies después (pueden depender de mocks)
            for (SpyField spyField : metadata.getSpyFields()) {
                Object spyInstance = createSpy(spyField);
                fieldInstances.put(spyField.getField(), spyInstance);
            }
            
            // Crear real beans si es necesario
            for (RealBeanField realBeanField : metadata.getRealBeanFields()) {
                Object realBeanInstance = createRealBean(realBeanField);
                fieldInstances.put(realBeanField.getField(), realBeanInstance);
            }
        }
        
        private Object createMock(MockField mockField) throws Exception {
            Mock mockAnnotation = mockField.getAnnotation();
            Class<?> type = mockField.getField().getType();
            
            Mockito.reset(type); // Reset si ya existe
            
            if (mockAnnotation.name().isEmpty()) {
                return Mockito.mock(type);
            } else {
                return Mockito.mock(type, mockAnnotation.name());
            }
        }
        
        private Object createSpy(SpyField spyField) throws Exception {
            Spy spyAnnotation = spyField.getAnnotation();
            Class<?> type = spyField.getField().getType();
            
            // Determinar si usar implementación real o mock
            boolean useRealImpl = spyAnnotation.realImplementation() && 
                                  warmupTest.mode() != TestMode.UNIT;
            
            if (useRealImpl) {
                // Crear spy sobre implementación real
                Object realInstance = instantiateRealImplementation(type, spyAnnotation);
                return Mockito.spy(realInstance);
            } else {
                // Crear spy sobre mock
                Object mockInstance = Mockito.mock(type);
                return Mockito.spy(mockInstance);
            }
        }
        
        private Object createRealBean(RealBeanField realBeanField) throws Exception {
            RealBean realBeanAnnotation = realBeanField.getAnnotation();
            Class<?> type = realBeanField.getField().getType();
            
            // En un contexto real, aquí buscaríamos en el ApplicationContext
            // Por ahora, instanciamos directamente
            return instantiateRealImplementation(type, null);
        }
        
        private Object instantiateRealImplementation(Class<?> type, Spy spyAnnotation) throws Exception {
            try {
                if (spyAnnotation != null && spyAnnotation.implementation() != Object.class) {
                    type = spyAnnotation.implementation();
                }
                
                // Intentar constructor sin argumentos primero
                return type.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // Si no hay constructor sin args, usar ObjectInstantiator
                ObjectInstantiator instantiator = new MockitoInstantiator();
                return instantiator.newInstance(type);
            }
        }
        
        private void performDependencyInjection(TestClassMetadata metadata) {
            // Inyectar mocks en spies basándose en tipos compatibles
            for (SpyField spyField : metadata.getSpyFields()) {
                Object spyInstance = fieldInstances.get(spyField.getField());
                if (spyInstance == null) continue;
                
                Map<Class<?>, Object> compatibleMocks = findCompatibleMocks(spyField, metadata);
                
                if (!compatibleMocks.isEmpty()) {
                    injector.injectDependencies(spyInstance, compatibleMocks);
                }
            }
        }
        
        private Map<Class<?>, Object> findCompatibleMocks(SpyField spyField, TestClassMetadata metadata) {
            Map<Class<?>, Object> compatibleMocks = new HashMap<>();
            
            for (MockField mockField : metadata.getMockFields()) {
                Object mockInstance = fieldInstances.get(mockField.getField());
                if (mockInstance == null) continue;
                
                Class<?> mockType = mockField.getField().getType();
                Class<?> spyType = spyField.getField().getType();
                
                // Verificar compatibilidad de tipos
                if (isCompatible(mockType, spyType)) {
                    compatibleMocks.put(mockType, mockInstance);
                }
            }
            
            return compatibleMocks;
        }
        
        private boolean isCompatible(Class<?> mockType, Class<?> spyType) {
            // Implementación simplificada - en un caso real sería más sofisticada
            return spyType.isAssignableFrom(mockType) || mockType.isAssignableFrom(spyType);
        }
        
        private void bindInstancesToTest(Object testInstance) {
            try {
                for (Map.Entry<Field, Object> entry : fieldInstances.entrySet()) {
                    Field field = entry.getKey();
                    Object value = entry.getValue();
                    
                    field.setAccessible(true);
                    field.set(testInstance, value);
                }
            } catch (Exception e) {
                throw new WarmupTestException("Error binding instances to test", e);
            }
        }
    }
    
    /**
     * Metadata de una clase de test con sus campos anotados.
     */
    private static class TestClassMetadata {
        private final List<MockField> mockFields = new ArrayList<>();
        private final List<SpyField> spyFields = new ArrayList<>();
        private final List<RealBeanField> realBeanFields = new ArrayList<>();
        private final Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
        
        public void addMockField(Field field, Mock annotation) {
            mockFields.add(new MockField(field, annotation));
        }
        
        public void addSpyField(Field field, Spy annotation) {
            spyFields.add(new SpyField(field, annotation));
        }
        
        public void addRealBeanField(Field field, RealBean annotation) {
            realBeanFields.add(new RealBeanField(field, annotation));
        }
        
        public List<MockField> getMockFields() {
            return Collections.unmodifiableList(mockFields);
        }
        
        public List<SpyField> getSpyFields() {
            return Collections.unmodifiableList(spyFields);
        }
        
        public List<RealBeanField> getRealBeanFields() {
            return Collections.unmodifiableList(realBeanFields);
        }
    }
    
    /**
     * Wrapper para un campo con anotación @Mock.
     */
    private static class MockField {
        private final Field field;
        private final Mock annotation;
        
        public MockField(Field field, Mock annotation) {
            this.field = field;
            this.annotation = annotation;
        }
        
        public Field getField() { return field; }
        public Mock getAnnotation() { return annotation; }
    }
    
    /**
     * Wrapper para un campo con anotación @Spy.
     */
    private static class SpyField {
        private final Field field;
        private final Spy annotation;
        
        public SpyField(Field field, Spy annotation) {
            this.field = field;
            this.annotation = annotation;
        }
        
        public Field getField() { return field; }
        public Spy getAnnotation() { return annotation; }
    }
    
    /**
     * Wrapper para un campo con anotación @RealBean.
     */
    private static class RealBeanField {
        private final Field field;
        private final RealBean annotation;
        
        public RealBeanField(Field field, RealBean annotation) {
            this.field = field;
            this.annotation = annotation;
        }
        
        public Field getField() { return field; }
        public RealBean getAnnotation() { return annotation; }
    }
}