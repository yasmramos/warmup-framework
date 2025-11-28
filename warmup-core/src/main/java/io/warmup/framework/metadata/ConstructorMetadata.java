package io.warmup.framework.metadata;

import java.util.*;
import java.lang.annotation.Annotation;

/**
 * Metadata de constructor pre-computada para eliminar reflexión.
 * 
 * Esta clase contiene información de constructores que normalmente
 * sería obtenida mediante getDeclaredConstructors(), sin reflexión.
 */
public class ConstructorMetadata {
    
    private String simpleName;
    private String qualifiedName;
    private List<ParameterMetadata> parameters = new ArrayList<>();
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isProtected;
    private boolean isPackagePrivate;
    private Map<String, String> annotations = new HashMap<>();
    
    // 🔄 CAMPOS CACHÉ PARA ACCESO RÁPIDO
    private volatile Integer parameterCount = null;
    private volatile Boolean hasInjectParameters = null;
    private volatile Boolean hasNamedParameters = null;
    
    /**
     * Constructor por defecto
     */
    public ConstructorMetadata() {
        // Constructor vacío para deserialización
    }
    
    /**
     * Constructor con información básica
     */
    public ConstructorMetadata(String simpleName, String qualifiedName) {
        this.simpleName = simpleName;
        this.qualifiedName = qualifiedName;
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
    
    public List<ParameterMetadata> getParameters() {
        return new ArrayList<>(parameters);
    }
    
    public void setParameters(List<ParameterMetadata> parameters) {
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        invalidateParameterCache(); // Cambios en parámetros afectan cache
    }
    
    public void addParameter(ParameterMetadata parameter) {
        if (parameter != null) {
            parameters.add(parameter);
            invalidateParameterCache();
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
    
    public boolean isPackagePrivate() {
        return isPackagePrivate;
    }
    
    public void setPackagePrivate(boolean packagePrivate) {
        isPackagePrivate = packagePrivate;
    }
    
    public Map<String, String> getAnnotations() {
        return new HashMap<>(annotations);
    }
    
    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations != null ? annotations : new HashMap<>();
    }
    
    public void addAnnotation(String annotationName, String annotationValue) {
        if (annotationName != null) {
            this.annotations.put(annotationName, annotationValue != null ? annotationValue : "");
        }
    }
    
    // 🚀 MÉTODOS DE UTILIDAD PARA ELIMINAR REFLEXIÓN
    
    /**
     * 🚀 REEMPLAZA: constructor.getParameterCount()
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
     * 🚀 REEMPLAZA: constructor.getParameterTypes()
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
     * 🚀 REEMPLAZA: constructor.getParameterAnnotations()
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
     * 🚀 REEMPLAZA: Arrays.stream(constructor.getParameterTypes()).findFirst()
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
     * 🚀 REEMPLAZA: Arrays.stream(constructor.getParameterAnnotations()).anyMatch()
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
     * 🚀 REEMPLAZA: constructor.getParameterTypes().length > 0
     * 
     * Verifica si el constructor tiene parámetros
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    
    /**
     * 🚀 Verifica si el constructor tiene parámetros inyectables
     */
    public boolean hasInjectableParameters() {
        if (hasInjectParameters == null) {
            hasInjectParameters = hasParameterWithAnnotation("io.warmup.framework.annotation.Inject") ||
                                  hasParameterWithAnnotation("io.warmup.framework.annotation.Named") ||
                                  hasParameterWithAnnotation("io.warmup.framework.annotation.Value") ||
                                  hasLikelyInjectableParameterTypes();
        }
        return hasInjectParameters;
    }
    
    /**
     * 🚀 Verifica si algún parámetro es probablemente inyectable
     */
    private boolean hasLikelyInjectableParameterTypes() {
        for (ParameterMetadata param : parameters) {
            String paramType = param.getQualifiedType();
            if (isLikelyInjectableType(paramType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 🚀 Verifica si un tipo probablemente requiere inyección
     */
    private boolean isLikelyInjectableType(String typeName) {
        if (typeName == null) {
            return false;
        }
        
        // No son inyectables: tipos primitivos, String, tipos de java.lang (excepto Object)
        if (typeName.startsWith("int") || typeName.startsWith("long") || 
            typeName.startsWith("double") || typeName.startsWith("float") ||
            typeName.startsWith("boolean") || typeName.startsWith("char") ||
            typeName.startsWith("byte") || typeName.startsWith("short") ||
            typeName.equals("java.lang.String") || 
            typeName.equals("java.lang.Integer") || 
            typeName.equals("java.lang.Long") ||
            typeName.equals("java.lang.Double") || 
            typeName.equals("java.lang.Float") ||
            typeName.equals("java.lang.Boolean") || 
            typeName.equals("java.lang.Character") ||
            typeName.equals("java.lang.Byte") || 
            typeName.equals("java.lang.Void")) {
            return false;
        }
        
        // Probablemente inyectable: clases en paquetes de aplicación
        return !typeName.startsWith("java.") && !typeName.startsWith("javax.");
    }
    
    /**
     * 🚀 Verifica si el constructor tiene parámetros @Named
     */
    public boolean hasNamedParameters() {
        if (hasNamedParameters == null) {
            hasNamedParameters = hasParameterWithAnnotation("io.warmup.framework.annotation.Named");
        }
        return hasNamedParameters;
    }
    
    /**
     * 🚀 REEMPLAZA: constructor.getAnnotation(annotationType)
     * 
     * Verifica si el constructor tiene una anotación específica
     */
    public boolean hasAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        return annotations.containsKey(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: constructor.getAnnotation(annotationType).value()
     * 
     * Obtiene valor de una anotación del constructor
     */
    public String getAnnotationValue(String annotationName) {
        if (annotationName == null) {
            return null;
        }
        return annotations.get(annotationName);
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
     * Verifica si es público
     */
    public boolean isAccessible() {
        return isPublic || isProtected || isPackagePrivate;
    }
    
    // 🔄 MÉTODOS DE CACHE MANAGEMENT
    
    /**
     * Invalida el cache de parámetros
     */
    private void invalidateParameterCache() {
        parameterCount = null;
        hasInjectParameters = null;
        hasNamedParameters = null;
    }
    
    // 📊 MÉTODOS DE ESTADÍSTICAS
    
    /**
     * Obtiene estadísticas del constructor
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("simpleName", simpleName);
        stats.put("qualifiedName", qualifiedName);
        stats.put("parameterCount", getParameterCount());
        stats.put("hasParameters", hasParameters());
        stats.put("hasInjectableParameters", hasInjectableParameters());
        stats.put("hasNamedParameters", hasNamedParameters());
        stats.put("accessibility", getAccessibility());
        stats.put("isAccessible", isAccessible());
        stats.put("annotationCount", annotations.size());
        
        // Estadísticas de parámetros
        List<String> paramTypes = getParameterTypes();
        stats.put("paramTypes", paramTypes);
        stats.put("paramTypesCount", paramTypes.size());
        
        return stats;
    }
    
    @Override
    public String toString() {
        return "ConstructorMetadata{" +
                "qualifiedName='" + qualifiedName + '\'' +
                ", parameterCount=" + getParameterCount() +
                ", hasInjectableParameters=" + hasInjectableParameters() +
                ", accessibility='" + getAccessibility() + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ConstructorMetadata that = (ConstructorMetadata) obj;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
    
    // 🔧 METHODS FOR COMPATIBILITY WITH io.warmup.framework.core.metadata.ConstructorMetadata
    
    /**
     * Get parameter types as array (for compatibility)
     */
    public String[] getParameterTypesArray() {
        List<String> paramTypes = getParameterTypes();
        return paramTypes != null ? paramTypes.toArray(new String[0]) : new String[0];
    }
    
    /**
     * Get exception types (returns empty array for compatibility)
     */
    public String[] getExceptionTypes() {
        return new String[0]; // Not stored in this implementation
    }
    
    /**
     * Get modifiers (returns 0 for compatibility)
     */
    public int getModifiers() {
        int modifiers = 0;
        if (isPublic) modifiers |= java.lang.reflect.Modifier.PUBLIC;
        if (isPrivate) modifiers |= java.lang.reflect.Modifier.PRIVATE;
        if (isProtected) modifiers |= java.lang.reflect.Modifier.PROTECTED;
        return modifiers;
    }
    
    /**
     * Get annotations as array (for compatibility)
     */
    public String[] getAnnotationsArray() {
        if (annotations == null || annotations.isEmpty()) {
            return new String[0];
        }
        return annotations.keySet().toArray(new String[0]);
    }
}