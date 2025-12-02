package io.warmup.test.core;

import io.warmup.test.annotation.*;
import io.warmup.test.exception.WarmupTestException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Configurador automático que aplica configuraciones específicas basadas
 * en las anotaciones de los campos de test.
 */
class AutoConfigurer {
    
    /**
     * Aplicar configuración automática basada en las anotaciones.
     */
    public void configure(Object testInstance, TestClassMetadata metadata) {
        // Configurar cada tipo de campo según sus anotaciones
        for (MockField mockField : metadata.getMockFields()) {
            configureMock(testInstance, mockField);
        }
        
        for (SpyField spyField : metadata.getSpyFields()) {
            configureSpy(testInstance, spyField);
        }
        
        for (RealBeanField realBeanField : metadata.getRealBeanFields()) {
            configureRealBean(testInstance, realBeanField);
        }
    }
    
    private void configureMock(Object testInstance, MockField mockField) {
        Mock mockAnnotation = mockField.getAnnotation();
        
        if (mockAnnotation.config().verbose()) {
            enableVerboseMocking(mockField.getField().getType());
        }
        
        // Aplicar configuración específica del mock si es necesario
        applyMockSpecificConfiguration(mockField, mockAnnotation.config());
    }
    
    private void configureSpy(Object testInstance, SpyField spyField) {
        Spy spyAnnotation = spyField.getAnnotation();
        
        // Configurar comportamiento específico del spy
        applySpySpecificConfiguration(spyField, spyAnnotation.config());
        
        // Si tiene @InjectMocks explícito, asegurar inyección de dependencias
        try {
            Field field = spyField.getField();
            field.setAccessible(true);
            Object spyInstance = field.get(testInstance);
            
            // Agregar listener para tracking de llamadas si está configurado
            if (spyAnnotation.config().trackCalls()) {
                addCallTracking(spyInstance, spyAnnotation.config());
            }
        } catch (Exception e) {
            throw new WarmupTestException("Error configuring spy: " + spyField.getField().getName(), e);
        }
    }
    
    private void configureRealBean(Object testInstance, RealBeanField realBeanField) {
        // Configurar bean real si es necesario
        // En un caso real, aquí buscaríamos en el contexto de la aplicación
    }
    
    private void enableVerboseMocking(Class<?> type) {
        // Habilitar logging verbose para el tipo específico
        System.setProperty("mockito.verbose." + type.getName(), "true");
    }
    
    private void applyMockSpecificConfiguration(MockField mockField, MockConfig config) {
        // Aplicar configuraciones específicas basadas en MockConfig
        // Ejemplo: configurar serialización, naming, etc.
        
        if (config.serializable()) {
            // Configurar mock como serializable
            // Mockito.mock(type, withSettings().serializable())
        }
    }
    
    private void applySpySpecificConfiguration(SpyField spyField, SpyConfig config) {
        // Aplicar configuraciones específicas basadas en SpyConfig
        // Ejemplo: configurar constructor específico, logging, etc.
        
        if (config.logLevel() != null && config.logLevel() != io.warmup.test.config.LogLevel.NONE) {
            // Configurar logging para el spy
            enableSpyLogging(spyField.getField().getType(), config.logLevel());
        }
    }
    
    private void enableSpyLogging(Class<?> type, io.warmup.test.config.LogLevel logLevel) {
        // Habilitar logging para el spy según el nivel especificado
        System.setProperty("spy.logging." + type.getName(), logLevel.name());
    }
    
    private void addCallTracking(Object spyInstance, SpyConfig config) {
        // Agregar tracking de llamadas para el spy
        // Esto podría usar listeners de Mockito o proxies personalizados
        
        if (config.trackCalls()) {
            // Implementar tracking de llamadas
            // Ejemplo: registrar cada llamada para verificación posterior
        }
    }
}

/**
 * Detector de problemas comunes en configuraciones de test.
 */
class ProblemDetector {
    
    /**
     * Detectar problemas comunes en la configuración del test.
     */
    public List<String> detectProblems(TestClassMetadata metadata) {
        List<String> problems = new ArrayList<>();
        
        // Verificar dependencias faltantes
        detectMissingDependencies(metadata, problems);
        
        // Verificar dependencias circulares
        detectCircularDependencies(metadata, problems);
        
        // Verificar tipos incompatibles
        detectIncompatibleTypes(metadata, problems);
        
        // Verificar configuración incorrecta
        detectMisconfiguration(metadata, problems);
        
        return problems;
    }
    
    private void detectMissingDependencies(TestClassMetadata metadata, List<String> problems) {
        for (SpyField spyField : metadata.getSpyFields()) {
            Class<?> spyType = spyField.getField().getType();
            
            // Simular análisis de constructor y setters
            Set<Class<?>> requiredTypes = getRequiredTypes(spyType);
            
            for (Class<?> requiredType : requiredTypes) {
                boolean hasCompatibleMock = metadata.getMockFields().stream()
                        .anyMatch(mockField -> isCompatible(requiredType, mockField.getField().getType()));
                
                if (!hasCompatibleMock) {
                    problems.add(String.format(
                        "Missing dependency for %s: %s not found in test class. " +
                        "Suggested fix: Add '@Mock %s %s;' or set autoMock=true in @WarmupTest",
                        spyType.getSimpleName(), 
                        requiredType.getSimpleName(),
                        requiredType.getSimpleName(),
                        toCamelCase(requiredType.getSimpleName())
                    ));
                }
            }
        }
    }
    
    private void detectCircularDependencies(TestClassMetadata metadata, List<String> problems) {
        // Implementación simplificada - detectar dependencias circulares obvias
        for (SpyField spyField : metadata.getSpyFields()) {
            Class<?> spyType = spyField.getField().getType();
            
            for (MockField mockField : metadata.getMockFields()) {
                Class<?> mockType = mockField.getField().getType();
                
                if (spyType.equals(mockType) || spyType.isAssignableFrom(mockType)) {
                    problems.add(String.format(
                        "Potential circular dependency between %s and %s. " +
                        "Consider using @RealBean for one of them.",
                        spyType.getSimpleName(),
                        mockType.getSimpleName()
                    ));
                }
            }
        }
    }
    
    private void detectIncompatibleTypes(TestClassMetadata metadata, List<String> problems) {
        // Verificar tipos incompatibles en las anotaciones
        for (MockField mockField : metadata.getMockFields()) {
            Class<?> mockType = mockField.getField().getType();
            
            if (mockType.isPrimitive()) {
                problems.add(String.format(
                    "Cannot create @Mock for primitive type %s. Consider using wrapper class.",
                    mockType.getSimpleName()
                ));
            }
        }
    }
    
    private void detectMisconfiguration(TestClassMetadata metadata, List<String> problems) {
        // Verificar configuraciones incorrectas
        for (SpyField spyField : metadata.getSpyFields()) {
            Spy spyAnnotation = spyField.getAnnotation();
            
            if (spyAnnotation.realImplementation() && spyAnnotation.implementation() != Object.class) {
                // Verificar que la implementación especificada sea compatible
                Class<?> spyType = spyField.getField().getType();
                Class<?> implType = spyAnnotation.implementation();
                
                if (!spyType.isAssignableFrom(implType)) {
                    problems.add(String.format(
                        "Implementation %s is not compatible with spy type %s",
                        implType.getSimpleName(),
                        spyType.getSimpleName()
                    ));
                }
            }
        }
    }
    
    private Set<Class<?>> getRequiredTypes(Class<?> spyType) {
        Set<Class<?>> requiredTypes = new HashSet<>();
        
        try {
            // Analizar constructor principal
            Arrays.stream(spyType.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .ifPresent(ctor -> {
                    for (Class<?> paramType : ctor.getParameterTypes()) {
                        requiredTypes.add(paramType);
                    }
                });
            
            // Analizar setters
            for (Method method : spyType.getDeclaredMethods()) {
                if (isSetterMethod(method)) {
                    requiredTypes.add(method.getParameterTypes()[0]);
                }
            }
            
        } catch (Exception e) {
            // Si no podemos analizar, asumir que no hay requisitos específicos
        }
        
        return requiredTypes;
    }
    
    private boolean isCompatible(Class<?> required, Class<?> available) {
        return required.isAssignableFrom(available) || available.isAssignableFrom(required);
    }
    
    private String toCamelCase(String className) {
        if (className == null || className.isEmpty()) {
            return "";
        }
        
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    
    private boolean isSetterMethod(Method method) {
        return method.getName().startsWith("set") 
               && method.getParameterCount() == 1
               && method.getReturnType() == void.class;
    }
}