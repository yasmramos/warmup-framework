package io.warmup.framework.metadata;

import java.util.*;
import java.lang.annotation.Annotation;

/**
 * Metadata de campo pre-computada para eliminar reflexión.
 * 
 * Esta clase contiene información de campos que normalmente
 * sería obtenida mediante getDeclaredFields(), sin reflexión.
 */
public class FieldMetadata {
    
    private String simpleName;
    private String qualifiedName;
    private String type;
    private String qualifiedType;
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isProtected;
    private boolean isStatic;
    private boolean isFinal;
    private boolean isTransient;
    private boolean isVolatile;
    private String defaultValue;
    private boolean hasDefaultValue;
    private Map<String, String> annotations = new HashMap<>();
    
    // 🔄 CAMPOS CACHÉ PARA ACCESO RÁPIDO
    private volatile Boolean isInjectable = null;
    private volatile Boolean isNamed = null;
    private volatile Boolean hasValueAnnotation = null;
    
    /**
     * Constructor por defecto
     */
    public FieldMetadata() {
        // Constructor vacío para deserialización
    }
    
    /**
     * Constructor con información básica
     */
    public FieldMetadata(String simpleName, String type, String qualifiedType) {
        this.simpleName = simpleName;
        this.type = type;
        this.qualifiedType = qualifiedType;
        this.qualifiedName = simpleName + ":" + qualifiedType;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getQualifiedType() {
        return qualifiedType;
    }
    
    public void setQualifiedType(String qualifiedType) {
        this.qualifiedType = qualifiedType;
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
    
    public boolean isTransient() {
        return isTransient;
    }
    
    public void setTransient(boolean aTransient) {
        isTransient = aTransient;
    }
    
    public boolean isVolatile() {
        return isVolatile;
    }
    
    public void setVolatile(boolean aVolatile) {
        isVolatile = aVolatile;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        this.hasDefaultValue = defaultValue != null;
    }
    
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }
    
    public void setHasDefaultValue(boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
    }
    
    public Map<String, String> getAnnotations() {
        return new HashMap<>(annotations);
    }
    
    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations != null ? annotations : new HashMap<>();
        invalidateFieldCache();
    }
    
    public void addAnnotation(String annotationName, String annotationValue) {
        if (annotationName != null) {
            this.annotations.put(annotationName, annotationValue != null ? annotationValue : "");
            invalidateFieldCache();
        }
    }
    
    // 🚀 MÉTODOS DE UTILIDAD PARA ELIMINAR REFLEXIÓN
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(Inject.class)
     * 
     * Verifica si el campo es inyectable
     */
    public boolean isInjectable() {
        if (isInjectable == null) {
            isInjectable = hasAnnotation("io.warmup.framework.annotation.Inject") ||
                           hasAnnotation("io.warmup.framework.annotation.Autowired") ||
                           isLikelyInjectableType();
        }
        return isInjectable;
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(Named.class)
     * 
     * Verifica si el campo tiene @Named
     */
    public boolean isNamed() {
        if (isNamed == null) {
            isNamed = hasAnnotation("io.warmup.framework.annotation.Named");
        }
        return isNamed;
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(Value.class)
     * 
     * Verifica si el campo tiene @Value
     */
    public boolean hasValueAnnotation() {
        if (hasValueAnnotation == null) {
            hasValueAnnotation = hasAnnotation("io.warmup.framework.annotation.Value");
        }
        return hasValueAnnotation;
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(annotationType)
     * 
     * Verifica si el campo tiene una anotación específica
     */
    public boolean hasAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        return annotations.containsKey(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(annotationType).value()
     * 
     * Obtiene valor de una anotación del campo
     */
    public String getAnnotationValue(String annotationName) {
        if (annotationName == null) {
            return null;
        }
        return annotations.get(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(Named.class).value()
     * 
     * Obtiene el valor de @Named
     */
    public String getNamedValue() {
        return getAnnotationValue("io.warmup.framework.annotation.Named");
    }
    
    /**
     * 🚀 REEMPLAZA: field.getAnnotation(Value.class).value()
     * 
     * Obtiene el valor de @Value
     */
    public String getValueAnnotation() {
        return getAnnotationValue("io.warmup.framework.annotation.Value");
    }
    
    /**
     * 🚀 Verifica si un tipo probablemente requiere inyección
     */
    private boolean isLikelyInjectableType() {
        if (qualifiedType == null) {
            return false;
        }
        
        // No son inyectables: tipos primitivos, String, tipos básicos de Java
        if (qualifiedType.startsWith("int") || qualifiedType.startsWith("long") || 
            qualifiedType.startsWith("double") || qualifiedType.startsWith("float") ||
            qualifiedType.startsWith("boolean") || qualifiedType.startsWith("char") ||
            qualifiedType.startsWith("byte") || qualifiedType.startsWith("short") ||
            qualifiedType.equals("java.lang.String") || 
            qualifiedType.equals("java.lang.Integer") || 
            qualifiedType.equals("java.lang.Long") ||
            qualifiedType.equals("java.lang.Double") || 
            qualifiedType.equals("java.lang.Float") ||
            qualifiedType.equals("java.lang.Boolean") || 
            qualifiedType.equals("java.lang.Character") ||
            qualifiedType.equals("java.lang.Byte") || 
            qualifiedType.equals("java.lang.Void")) {
            return false;
        }
        
        // Probablemente inyectable: clases en paquetes de aplicación (no java.*)
        return !qualifiedType.startsWith("java.") && !qualifiedType.startsWith("javax.");
    }
    
    /**
     * 🚀 Verifica si es un campo estático final (constante)
     */
    public boolean isConstant() {
        return isStatic && isFinal && hasDefaultValue;
    }
    
    /**
     * 🚀 Verifica si el campo es accesible
     */
    public boolean isAccessible() {
        return isPublic || isProtected || isPackagePrivate();
    }
    
    /**
     * 🚀 Obtiene información de accesibilidad
     */
    public String getAccessibility() {
        if (isPublic) return "public";
        if (isPrivate) return "private";
        if (isProtected) return "protected";
        return "package-private";
    }
    
    /**
     * 🚀 Verifica si es package-private
     */
    private boolean isPackagePrivate() {
        return !isPublic && !isPrivate && !isProtected;
    }
    
    /**
     * 🚀 Obtiene el valor por defecto formateado
     */
    public String getFormattedDefaultValue() {
        if (!hasDefaultValue || defaultValue == null) {
            return "null";
        }
        
        // Formatear valores según el tipo
        if (qualifiedType.equals("java.lang.String")) {
            return "\"" + defaultValue + "\"";
        } else if (qualifiedType.equals("java.lang.Character") || qualifiedType.equals("char")) {
            return "'" + defaultValue + "'";
        } else if (defaultValue.equals("true") || defaultValue.equals("false")) {
            return defaultValue;
        } else if (isNumericType()) {
            return defaultValue;
        } else {
            // Otros tipos: intentar inferir formato
            return defaultValue;
        }
    }
    
    /**
     * 🚀 Verifica si es un tipo numérico
     */
    private boolean isNumericType() {
        return qualifiedType.startsWith("int") || qualifiedType.startsWith("long") || 
               qualifiedType.startsWith("double") || qualifiedType.startsWith("float") ||
               qualifiedType.startsWith("short") || qualifiedType.startsWith("byte") ||
               qualifiedType.equals("java.lang.Integer") || 
               qualifiedType.equals("java.lang.Long") ||
               qualifiedType.equals("java.lang.Double") || 
               qualifiedType.equals("java.lang.Float") ||
               qualifiedType.equals("java.lang.Short") || 
               qualifiedType.equals("java.lang.Byte");
    }
    
    // 🔄 MÉTODOS DE CACHE MANAGEMENT
    
    /**
     * Invalida el cache de campos
     */
    private void invalidateFieldCache() {
        isInjectable = null;
        isNamed = null;
        hasValueAnnotation = null;
    }
    
    // 📊 MÉTODOS DE ESTADÍSTICAS
    
    /**
     * Obtiene estadísticas del campo
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("simpleName", simpleName);
        stats.put("qualifiedName", qualifiedName);
        stats.put("type", type);
        stats.put("qualifiedType", qualifiedType);
        stats.put("isInjectable", isInjectable());
        stats.put("isNamed", isNamed());
        stats.put("hasValueAnnotation", hasValueAnnotation());
        stats.put("hasDefaultValue", hasDefaultValue);
        stats.put("isConstant", isConstant());
        stats.put("accessibility", getAccessibility());
        stats.put("isAccessible", isAccessible());
        stats.put("isStatic", isStatic);
        stats.put("isFinal", isFinal);
        stats.put("isTransient", isTransient);
        stats.put("isVolatile", isVolatile);
        stats.put("annotationCount", annotations.size());
        
        if (hasDefaultValue) {
            stats.put("defaultValue", defaultValue);
            stats.put("formattedDefaultValue", getFormattedDefaultValue());
        }
        
        return stats;
    }
    
    @Override
    public String toString() {
        return "FieldMetadata{" +
                "simpleName='" + simpleName + '\'' +
                ", qualifiedType='" + qualifiedType + '\'' +
                ", isInjectable=" + isInjectable() +
                ", isNamed=" + isNamed() +
                ", accessibility='" + getAccessibility() + '\'' +
                ", isConstant=" + isConstant() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FieldMetadata that = (FieldMetadata) obj;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
}