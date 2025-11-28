package io.warmup.framework.metadata;

import java.util.*;

import java.util.*;
import java.lang.annotation.Annotation;

/**
 * Metadata de método pre-computada para eliminar reflexión.
 * 
 * Esta clase contiene información de métodos que normalmente
 * sería obtenida mediante getDeclaredMethods(), sin reflexión.
 */
public class MethodMetadata {
    
    private String simpleName;
    private String qualifiedName;
    private String returnType;
    private String qualifiedReturnType;
    private List<ParameterMetadata> parameters = new ArrayList<>();
    private List<String> exceptions = new ArrayList<>();
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isProtected;
    private boolean isStatic;
    private boolean isFinal;
    private boolean isAbstract;
    private Map<String, String> annotations = new HashMap<>();
    
    // 🔄 CAMPOS CACHÉ PARA ACCESO RÁPIDO
    private volatile Integer parameterCount = null;
    private volatile Boolean isVoid = null;
    private volatile Boolean hasParameters = null;
    private volatile Boolean isBeanMethod = null;
    private volatile Boolean isAspectMethod = null;
    private volatile Boolean isHealthCheckMethod = null;
    private volatile String[] profileValues = null;
    
    /**
     * Constructor por defecto
     */
    public MethodMetadata() {
        // Constructor vacío para deserialización
    }
    
    /**
     * Constructor con información básica
     */
    public MethodMetadata(String simpleName, String returnType, String qualifiedReturnType) {
        this.simpleName = simpleName;
        this.returnType = returnType;
        this.qualifiedReturnType = qualifiedReturnType;
        this.qualifiedName = simpleName + "(" + qualifiedReturnType + ")";
    }
    
    // 📋 MÉTODOS BÁSICOS
    
    public String getSimpleName() {
        return simpleName;
    }
    
    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }
    
    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
        invalidateMethodCache();
    }
    
    public String getQualifiedReturnType() {
        return qualifiedReturnType;
    }
    
    public void setQualifiedReturnType(String qualifiedReturnType) {
        this.qualifiedReturnType = qualifiedReturnType;
        invalidateMethodCache();
    }
    
    public List<ParameterMetadata> getParameters() {
        return new ArrayList<>(parameters);
    }
    
    public void setParameters(List<ParameterMetadata> parameters) {
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        invalidateParameterCache();
    }
    
    public void addParameter(ParameterMetadata parameter) {
        if (parameter != null) {
            parameters.add(parameter);
            invalidateParameterCache();
        }
    }
    
    public List<String> getExceptions() {
        return new ArrayList<>(exceptions);
    }
    
    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions != null ? exceptions : new ArrayList<>();
    }
    
    public void addException(String exceptionName) {
        if (exceptionName != null && !exceptions.contains(exceptionName)) {
            exceptions.add(exceptionName);
        }
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    
    public boolean isProtected() {
        return isProtected;
    }
    
    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }
    
    public Map<String, String> getAnnotations() {
        return new HashMap<>(annotations);
    }
    
    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations != null ? annotations : new HashMap<>();
        invalidateMethodCache();
    }
    
    public void addAnnotation(String annotationName, String annotationValue) {
        if (annotationName != null) {
            this.annotations.put(annotationName, annotationValue != null ? annotationValue : "");
            invalidateMethodCache();
        }
    }
    
    // 🚀 MÉTODOS DE UTILIDAD PARA ELIMINAR REFLEXIÓN
    
    /**
     * 🚀 REEMPLAZA: method.getParameterCount()
     * 
     * Obtiene el número de parámetros
     */
    public int getParameterCount() {
        if (parameterCount == null) {
            parameterCount = parameters.size();
        }
        return parameterCount;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getParameterTypes()
     * 
     * Obtiene tipos de parámetros
     */
    public List<String> getParameterTypes() {
        List<String> types = new ArrayList<>();
        for (ParameterMetadata param : parameters) {
            types.add(param.getQualifiedType());
        }
        return types;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getParameterAnnotations()
     * 
     * Obtiene anotaciones de parámetros
     */
    public List<Map<String, String>> getParameterAnnotations() {
        List<Map<String, String>> annotations = new ArrayList<>();
        for (ParameterMetadata param : parameters) {
            annotations.add(param.getAnnotationsAsMap());
        }
        return annotations;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getReturnType().equals(void.class)
     * 
     * Verifica si el método retorna void
     */
    public boolean isVoid() {
        if (isVoid == null) {
            isVoid = "void".equals(returnType) || "java.lang.Void".equals(qualifiedReturnType);
        }
        return isVoid;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getParameterTypes().length == 0
     * 
     * Verifica si el método tiene parámetros
     */
    public boolean hasParameters() {
        if (hasParameters == null) {
            hasParameters = !parameters.isEmpty();
        }
        return hasParameters;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(Bean.class)
     * 
     * Verifica si el método es un @Bean method
     */
    public boolean isBeanMethod() {
        if (isBeanMethod == null) {
            isBeanMethod = hasAnnotation("io.warmup.framework.annotation.Bean");
        }
        return isBeanMethod;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(Aspect.class) || 
     *                method.getAnnotation(Before.class) || 
     *                method.getAnnotation(After.class) || 
     *                method.getAnnotation(Around.class)
     * 
     * Verifica si el método es un método de aspect
     */
    public boolean isAspectMethod() {
        if (isAspectMethod == null) {
            isAspectMethod = hasAnnotation("io.warmup.framework.annotation.Aspect") ||
                             hasAnnotation("io.warmup.framework.annotation.Before") ||
                             hasAnnotation("io.warmup.framework.annotation.After") ||
                             hasAnnotation("io.warmup.framework.annotation.Around");
        }
        return isAspectMethod;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(Health.class)
     * 
     * Verifica si el método es un health check
     */
    public boolean isHealthCheckMethod() {
        if (isHealthCheckMethod == null) {
            isHealthCheckMethod = hasAnnotation("io.warmup.framework.annotation.Health");
        }
        return isHealthCheckMethod;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(Profile.class)
     * 
     * Verifica si el método tiene @Profile
     */
    public boolean hasProfileAnnotation() {
        return hasAnnotation("io.warmup.framework.annotation.Profile");
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(Profile.class).value()
     * 
     * Obtiene valores de @Profile
     */
    public String[] getProfileValues() {
        if (profileValues == null) {
            String profileAnnotation = annotations.get("io.warmup.framework.annotation.Profile");
            if (profileAnnotation != null && profileAnnotation.startsWith("[")) {
                // Parse array format: ["dev", "test"]
                String content = profileAnnotation.substring(1, profileAnnotation.length() - 1);
                profileValues = content.split(",\\s*");
            } else if (profileAnnotation != null) {
                profileValues = new String[]{profileAnnotation};
            } else {
                profileValues = new String[0];
            }
        }
        return profileValues;
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(annotationType)
     * 
     * Verifica si el método tiene una anotación específica
     */
    public boolean hasAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        return annotations.containsKey(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(annotationType).value()
     * 
     * Obtiene valor de una anotación del método
     */
    public String getAnnotationValue(String annotationName) {
        if (annotationName == null) {
            return null;
        }
        return annotations.get(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: Arrays.stream(method.getParameterTypes()).findFirst()
     * 
     * Encuentra el primer parámetro de un tipo específico
     */
    public ParameterMetadata findParameterOfType(String parameterType) {
        if (parameterType == null) {
            return null;
        }
        return parameters.stream()
                .filter(param -> parameterType.equals(param.getQualifiedType()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 🚀 REEMPLAZA: Arrays.stream(method.getParameterAnnotations()).anyMatch()
     * 
     * Verifica si algún parámetro tiene una anotación específica
     */
    public boolean hasParameterWithAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        return parameters.stream()
                .anyMatch(param -> param.hasAnnotation(annotationName));
    }
    
    /**
     * 🚀 REEMPLAZA: method.getAnnotation(annotationType).value()
     * 
     * Obtiene valor de una anotación de parámetro
     */
    public String getParameterAnnotationValue(String parameterName, String annotationName) {
        if (parameterName == null || annotationName == null) {
            return null;
        }
        return parameters.stream()
                .filter(param -> parameterName.equals(param.getName()))
                .findFirst()
                .map(param -> param.getAnnotationValue(annotationName))
                .map(obj -> obj != null ? obj.toString() : null)
                .orElse(null);
    }
    
    /**
     * Obtiene información de accesibilidad
     */
    public String getAccessibility() {
        if (isPublic) return "public";
        if (isPrivate) return "private";
        if (isProtected) return "protected";
        return "package-private";
    }
    
    /**
     * Verifica si es accesible públicamente
     */
    public boolean isAccessible() {
        return isPublic || isProtected || isAbstract; // Métodos abstractos en interfaces son "accesibles"
    }
    
    /**
     * Verifica si es un getter (sin parámetros, retorna algo)
     */
    public boolean isGetter() {
        return !hasParameters() && !isVoid();
    }
    
    /**
     * Verifica si es un setter (un parámetro, retorna void)
     */
    public boolean isSetter() {
        return getParameterCount() == 1 && isVoid();
    }
    
    /**
     * Obtiene el nombre de la propiedad para getters/setters
     */
    public String getPropertyName() {
        String name = simpleName;
        if (name.startsWith("get") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else if (name.startsWith("is") && name.length() > 2) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        } else if (name.startsWith("set") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }
    
    // 🔄 MÉTODOS DE CACHE MANAGEMENT
    
    /**
     * Invalida el cache de parámetros
     */
    private void invalidateParameterCache() {
        parameterCount = null;
        hasParameters = null;
    }
    
    /**
     * Invalida el cache de métodos
     */
    private void invalidateMethodCache() {
        isVoid = null;
        isBeanMethod = null;
        isAspectMethod = null;
        isHealthCheckMethod = null;
        profileValues = null;
    }
    
    // 📊 MÉTODOS DE ESTADÍSTICAS
    
    /**
     * Obtiene estadísticas del método
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("simpleName", simpleName);
        stats.put("qualifiedName", qualifiedName);
        stats.put("returnType", returnType);
        stats.put("qualifiedReturnType", qualifiedReturnType);
        stats.put("parameterCount", getParameterCount());
        stats.put("hasParameters", hasParameters());
        stats.put("isVoid", isVoid());
        stats.put("isBeanMethod", isBeanMethod());
        stats.put("isAspectMethod", isAspectMethod());
        stats.put("isHealthCheckMethod", isHealthCheckMethod());
        stats.put("hasProfileAnnotation", hasProfileAnnotation());
        stats.put("accessibility", getAccessibility());
        stats.put("isAccessible", isAccessible());
        stats.put("isStatic", isStatic);
        stats.put("isFinal", isFinal);
        stats.put("isAbstract", isAbstract);
        stats.put("exceptionCount", exceptions.size());
        stats.put("annotationCount", annotations.size());
        
        // Estadísticas de parámetros
        List<String> paramTypes = getParameterTypes();
        stats.put("paramTypes", paramTypes);
        stats.put("paramTypesCount", paramTypes.size());
        
        return stats;
    }
    
    @Override
    public String toString() {
        return "MethodMetadata{" +
                "qualifiedName='" + qualifiedName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameterCount=" + getParameterCount() +
                ", isBeanMethod=" + isBeanMethod() +
                ", isAspectMethod=" + isAspectMethod() +
                ", accessibility='" + getAccessibility() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MethodMetadata that = (MethodMetadata) obj;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
    

    
    /**
     * Convierte un Method reflexivo a MethodMetadata legacy para compatibilidad
     */
    public static MethodMetadata fromReflectionMethod(java.lang.reflect.Method reflectionMethod) {
        if (reflectionMethod == null) {
            return null;
        }
        
        MethodMetadata method = new MethodMetadata(
            reflectionMethod.getName(),
            reflectionMethod.getReturnType().getSimpleName(),
            reflectionMethod.getReturnType().getName()
        );
        
        // Set properties
        method.simpleName = reflectionMethod.getName();
        method.qualifiedName = reflectionMethod.getName();
        method.returnType = reflectionMethod.getReturnType().getSimpleName();
        method.qualifiedReturnType = reflectionMethod.getReturnType().getName();
        method.parameterCount = reflectionMethod.getParameterCount();
        method.isPublic = java.lang.reflect.Modifier.isPublic(reflectionMethod.getModifiers());
        method.isPrivate = java.lang.reflect.Modifier.isPrivate(reflectionMethod.getModifiers());
        method.isProtected = java.lang.reflect.Modifier.isProtected(reflectionMethod.getModifiers());
        method.isStatic = java.lang.reflect.Modifier.isStatic(reflectionMethod.getModifiers());
        method.isFinal = java.lang.reflect.Modifier.isFinal(reflectionMethod.getModifiers());
        method.isAbstract = java.lang.reflect.Modifier.isAbstract(reflectionMethod.getModifiers());
        
        // Convert parameter types
        List<String> paramTypes = new ArrayList<>();
        List<ParameterMetadata> parameters = new ArrayList<>();
        Class<?>[] paramClasses = reflectionMethod.getParameterTypes();
        for (int i = 0; i < paramClasses.length; i++) {
            String paramTypeName = paramClasses[i].getName();
            paramTypes.add(paramTypeName);
            
            ParameterMetadata param = new ParameterMetadata("arg" + i, paramTypeName, false, false);
            parameters.add(param);
        }
        method.parameters.addAll(parameters);
        
        // Copy annotations from reflection
        for (java.lang.annotation.Annotation annotation : reflectionMethod.getAnnotations()) {
            method.annotations.put(annotation.annotationType().getName(), annotation.toString());
        }
        
        return method;
    }
}