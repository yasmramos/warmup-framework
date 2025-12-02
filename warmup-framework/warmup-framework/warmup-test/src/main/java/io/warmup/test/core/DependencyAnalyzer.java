package io.warmup.test.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Analizador de dependencias que detecta automáticamente las relaciones
 * entre mocks, spies y dependencias en los campos de una clase de test.
 */
class DependencyAnalyzer {
    
    private final Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
    
    /**
     * Analizar las dependencias entre los campos de una clase de test.
     */
    public void analyzeDependencies(TestClassMetadata metadata) {
        for (SpyField spyField : metadata.getSpyFields()) {
            analyzeSpyDependencies(spyField, metadata);
        }
        
        // Detectar dependencias circulares y resolverlas
        detectCircularDependencies(metadata);
    }
    
    private void analyzeSpyDependencies(SpyField spyField, TestClassMetadata metadata) {
        Class<?> spyType = spyField.getField().getType();
        Set<Class<?>> dependencies = new HashSet<>();
        
        // Analizar constructor del spy
        dependencies.addAll(analyzeConstructorDependencies(spyType));
        
        // Analizar setters del spy
        dependencies.addAll(analyzeSetterDependencies(spyType));
        
        // Analizar campos del spy
        dependencies.addAll(analyzeFieldDependencies(spyType));
        
        dependencyGraph.put(spyType, dependencies);
    }
    
    private Set<Class<?>> analyzeConstructorDependencies(Class<?> type) {
        Set<Class<?>> dependencies = new HashSet<>();
        
        try {
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            
            // Preferir constructor con más parámetros (más específico)
            Constructor<?> preferredConstructor = Arrays.stream(constructors)
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElse(null);
            
            if (preferredConstructor != null) {
                for (Class<?> paramType : preferredConstructor.getParameterTypes()) {
                    dependencies.add(paramType);
                }
            }
        } catch (Exception e) {
            // Constructor no accesible, ignorar
        }
        
        return dependencies;
    }
    
    private Set<Class<?>> analyzeSetterDependencies(Class<?> type) {
        Set<Class<?>> dependencies = new HashSet<>();
        
        for (Method method : type.getDeclaredMethods()) {
            if (isSetterMethod(method)) {
                Class<?> paramType = method.getParameterTypes()[0];
                dependencies.add(paramType);
            }
        }
        
        return dependencies;
    }
    
    private Set<Class<?>> analyzeFieldDependencies(Class<?> type) {
        Set<Class<?>> dependencies = new HashSet<>();
        
        for (Field field : type.getDeclaredFields()) {
            dependencies.add(field.getType());
        }
        
        return dependencies;
    }
    
    private boolean isSetterMethod(Method method) {
        return method.getName().startsWith("set") 
               && method.getParameterCount() == 1
               && method.getReturnType() == void.class;
    }
    
    private void detectCircularDependencies(TestClassMetadata metadata) {
        Map<Class<?>, List<Class<?>>> dependencyMap = buildDependencyMap(metadata);
        
        Set<Class<?>> cyclicClasses = findCyclicDependencies(dependencyMap);
        
        if (!cyclicClasses.isEmpty()) {
            // Resolver dependencias circulares automáticamente
            resolveCircularDependencies(cyclicClasses, dependencyMap);
        }
    }
    
    private Map<Class<?>, List<Class<?>>> buildDependencyMap(TestClassMetadata metadata) {
        Map<Class<?>, List<Class<?>>> dependencyMap = new HashMap<>();
        
        for (SpyField spyField : metadata.getSpyFields()) {
            Class<?> spyType = spyField.getField().getType();
            List<Class<?>> dependencies = new ArrayList<>();
            
            // Buscar mocks compatibles
            for (MockField mockField : metadata.getMockFields()) {
                Class<?> mockType = mockField.getField().getType();
                if (isCompatible(mockType, spyType)) {
                    dependencies.add(mockType);
                }
            }
            
            dependencyMap.put(spyType, dependencies);
        }
        
        return dependencyMap;
    }
    
    private Set<Class<?>> findCyclicDependencies(Map<Class<?>, List<Class<?>>> dependencyMap) {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> recStack = new HashSet<>();
        Set<Class<?>> cyclicClasses = new HashSet<>();
        
        for (Class<?> className : dependencyMap.keySet()) {
            if (!visited.contains(className)) {
                findCyclesDFS(className, dependencyMap, visited, recStack, cyclicClasses);
            }
        }
        
        return cyclicClasses;
    }
    
    private void findCyclesDFS(Class<?> node, Map<Class<?>, List<Class<?>>> dependencyMap,
                               Set<Class<?>> visited, Set<Class<?>> recStack, 
                               Set<Class<?>> cyclicClasses) {
        
        if (recStack.contains(node)) {
            cyclicClasses.add(node);
            return;
        }
        
        if (visited.contains(node)) {
            return;
        }
        
        visited.add(node);
        recStack.add(node);
        
        List<Class<?>> dependencies = dependencyMap.getOrDefault(node, Collections.emptyList());
        for (Class<?> dependency : dependencies) {
            if (dependencyMap.containsKey(dependency)) {
                findCyclesDFS(dependency, dependencyMap, visited, recStack, cyclicClasses);
            }
        }
        
        recStack.remove(node);
    }
    
    private void resolveCircularDependencies(Set<Class<?>> cyclicClasses, 
                                           Map<Class<?>, List<Class<?>>> dependencyMap) {
        
        for (Class<?> cyclicClass : cyclicClasses) {
            List<Class<?>> dependencies = dependencyMap.get(cyclicClass);
            if (dependencies != null && dependencies.size() > 1) {
                // Mantener solo la primera dependencia para romper el ciclo
                List<Class<?>> resolvedDependencies = dependencies.subList(0, 1);
                dependencyMap.put(cyclicClass, resolvedDependencies);
            }
        }
    }
    
    private boolean isCompatible(Class<?> mockType, Class<?> spyType) {
        // Verificar compatibilidad de tipos
        return spyType.isAssignableFrom(mockType) || mockType.isAssignableFrom(spyType);
    }
    
    /**
     * Obtener las dependencias de una clase específica.
     */
    public Set<Class<?>> getDependencies(Class<?> type) {
        return dependencyGraph.getOrDefault(type, Collections.emptySet());
    }
    
    /**
     * Verificar si una clase tiene dependencias circulares.
     */
    public boolean hasCircularDependencies(Class<?> type) {
        // Implementación simplificada
        Set<Class<?>> dependencies = getDependencies(type);
        return dependencies.stream().anyMatch(dep -> dep.equals(type));
    }
}