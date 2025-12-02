package io.warmup.framework.metadata;

import java.util.*;
import java.lang.annotation.Annotation;

/**
 * Metadata de clase pre-computada para eliminar reflexión.
 * 
 * Esta clase contiene toda la información de una clase que normalmente
 * sería obtenida mediante reflexión, pero pre-computada en tiempo de compilación
 * para acceso O(1) sin reflexión.
 */
public class ClassMetadata {
    
    private String className;
    private String simpleName;
    private String qualifiedName;
    private String packageName;
    private String superClass;
    private List<String> interfaces = new ArrayList<>();
    private Map<String, Map<String, String>> annotations = new HashMap<>();
    private List<ConstructorMetadata> constructors = new ArrayList<>();
    private List<MethodMetadata> methods = new ArrayList<>();
    private List<FieldMetadata> fields = new ArrayList<>();
    
    // 🔄 CAMPOS CACHÉ PARA ACCESO RÁPIDO
    private volatile Boolean hasProfileAnnotation = null;
    private volatile String[] profileValues = null;
    private volatile Boolean isComponent = null;
    private volatile Boolean isBean = null;
    private volatile Boolean isService = null;
    
    /**
     * Constructor por defecto
     */
    public ClassMetadata() {
        // Constructor vacío para deserialización
    }
    
    /**
     * Constructor con información básica
     */
    public ClassMetadata(String className, String simpleName, String packageName) {
        this.className = className;
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.qualifiedName = className;
    }
    
    // 📋 MÉTODOS BÁSICOS
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
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
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getSuperClass() {
        return superClass;
    }
    
    public void setSuperClass(String superClass) {
        this.superClass = superClass;
        invalidateProfileCache(); // Superclass puede afectar annotations
    }
    
    public List<String> getInterfaces() {
        return new ArrayList<>(interfaces);
    }
    
    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces != null ? interfaces : new ArrayList<>();
    }
    
    public void addInterface(String interfaceName) {
        if (interfaceName != null && !interfaces.contains(interfaceName)) {
            interfaces.add(interfaceName);
        }
    }
    
    public Map<String, Map<String, String>> getAnnotations() {
        return new HashMap<>(annotations);
    }
    
    public void setAnnotations(Map<String, Map<String, String>> annotations) {
        this.annotations = annotations != null ? annotations : new HashMap<>();
        invalidateProfileCache(); // Cambios en annotations afectan cache de perfil
    }
    
    public void addAnnotation(String annotationName, Map<String, String> values) {
        if (annotationName != null) {
            this.annotations.put(annotationName, values != null ? values : new HashMap<>());
            invalidateProfileCache(); // Cambios en annotations afectan cache de perfil
        }
    }
    
    public List<ConstructorMetadata> getConstructors() {
        return new ArrayList<>(constructors);
    }
    
    public void setConstructors(List<ConstructorMetadata> constructors) {
        this.constructors = constructors != null ? constructors : new ArrayList<>();
    }
    
    public void addConstructor(ConstructorMetadata constructor) {
        if (constructor != null) {
            constructors.add(constructor);
        }
    }
    
    public List<MethodMetadata> getMethods() {
        return new ArrayList<>(methods);
    }
    
    public void setMethods(List<MethodMetadata> methods) {
        this.methods = methods != null ? methods : new ArrayList<>();
    }
    
    public void addMethod(MethodMetadata method) {
        if (method != null) {
            methods.add(method);
        }
    }
    
    public List<FieldMetadata> getFields() {
        return new ArrayList<>(fields);
    }
    
    public void setFields(List<FieldMetadata> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }
    
    public void addField(FieldMetadata field) {
        if (field != null) {
            fields.add(field);
        }
    }
    
    // 🚀 MÉTODOS DE UTILIDAD PARA ELIMINAR REFLEXIÓN
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(Profile.class)
     * 
     * Verifica si la clase tiene @Profile annotation
     */
    public boolean hasProfileAnnotation() {
        if (hasProfileAnnotation == null) {
            hasProfileAnnotation = hasAnnotation("io.warmup.framework.annotation.Profile");
        }
        return hasProfileAnnotation;
    }
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(Profile.class).value()
     * 
     * Obtiene valores de @Profile annotation
     */
    public String[] getProfileValues() {
        if (profileValues == null) {
            Map<String, String> profileAnnotation = annotations.get("io.warmup.framework.annotation.Profile");
            if (profileAnnotation != null && profileAnnotation.containsKey("value")) {
                String value = profileAnnotation.get("value");
                if (value != null && value.startsWith("[") && value.endsWith("]")) {
                    // Parse array format: ["dev", "test"]
                    String content = value.substring(1, value.length() - 1);
                    profileValues = content.split(",\\s*");
                } else {
                    profileValues = new String[]{value};
                }
            } else {
                profileValues = new String[0];
            }
        }
        return profileValues;
    }
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(Component.class)
     * 
     * Verifica si la clase tiene @Component annotation
     */
    public boolean isComponent() {
        if (isComponent == null) {
            isComponent = hasAnnotation("io.warmup.framework.annotation.Component");
        }
        return isComponent;
    }
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(Bean.class)
     * 
     * Verifica si la clase tiene @Bean annotation
     */
    public boolean isBean() {
        if (isBean == null) {
            isBean = hasAnnotation("io.warmup.framework.annotation.Bean");
        }
        return isBean;
    }
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(Service.class)
     * 
     * Verifica si la clase tiene @Service annotation
     */
    public boolean isService() {
        if (isService == null) {
            isService = hasAnnotation("io.warmup.framework.annotation.Service");
        }
        return isService;
    }
    
    /**
     * 🚀 REEMPLAZA: Class.forName(className)
     * 
     * Verifica si la clase tiene una anotación específica
     */
    public boolean hasAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        return annotations.containsKey(annotationName);
    }
    
    /**
     * 🚀 REEMPLAZA: type.getAnnotation(annotationType)
     * 
     * Obtiene valores de una anotación específica
     */
    public Map<String, String> getAnnotationValues(String annotationName) {
        if (annotationName == null) {
            return new HashMap<>();
        }
        return annotations.getOrDefault(annotationName, new HashMap<>());
    }
    
    /**
     * 🚀 REEMPLAZA: type.getDeclaredConstructors().length
     * 
     * Obtiene el número de constructores
     */
    public int getConstructorCount() {
        return constructors.size();
    }
    
    /**
     * 🚀 REEMPLAZA: type.getDeclaredMethods().length
     * 
     * Obtiene el número de métodos
     */
    public int getMethodCount() {
        return methods.size();
    }
    
    /**
     * 🚀 REEMPLAZA: type.getDeclaredFields().length
     * 
     * Obtiene el número de campos
     */
    public int getFieldCount() {
        return fields.size();
    }
    
    /**
     * 🚀 REEMPLAZA: Arrays.asList(type.getInterfaces())
     * 
     * Verifica si la clase implementa una interfaz específica
     */
    public boolean implementsInterface(String interfaceName) {
        if (interfaceName == null) {
            return false;
        }
        return interfaces.contains(interfaceName);
    }
    
    /**
     * 🚀 REEMPLAZA: type.getSuperclass()
     * 
     * Verifica si la clase hereda de una clase específica
     */
    public boolean inheritsFrom(String superclassName) {
        if (superclassName == null) {
            return false;
        }
        return superclassName.equals(superClass);
    }
    
    // 🔄 MÉTODOS DE CACHE MANAGEMENT
    
    /**
     * Invalida el cache de perfiles
     */
    private void invalidateProfileCache() {
        hasProfileAnnotation = null;
        profileValues = null;
        isComponent = null;
        isBean = null;
        isService = null;
    }
    
    /**
     * Construye un ConstructorMetadata con información específica
     */
    public ConstructorMetadata findConstructorWithParameterCount(int paramCount) {
        return constructors.stream()
                .filter(ctor -> ctor.getParameterCount() == paramCount)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Encuentra un método por nombre
     */
    public MethodMetadata findMethodByName(String methodName) {
        if (methodName == null) {
            return null;
        }
        return methods.stream()
                .filter(method -> methodName.equals(method.getSimpleName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Encuentra un campo por nombre
     */
    public FieldMetadata findFieldByName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return fields.stream()
                .filter(field -> fieldName.equals(field.getSimpleName()))
                .findFirst()
                .orElse(null);
    }
    
    // 📊 MÉTODOS DE ESTADÍSTICAS
    
    /**
     * Obtiene estadísticas de la clase
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("className", className);
        stats.put("simpleName", simpleName);
        stats.put("packageName", packageName);
        stats.put("hasSuperClass", superClass != null);
        stats.put("interfaceCount", interfaces.size());
        stats.put("annotationCount", annotations.size());
        stats.put("constructorCount", constructors.size());
        stats.put("methodCount", methods.size());
        stats.put("fieldCount", fields.size());
        stats.put("hasProfileAnnotation", hasProfileAnnotation());
        stats.put("isComponent", isComponent());
        stats.put("isBean", isBean());
        stats.put("isService", isService());
        return stats;
    }
    
    @Override
    public String toString() {
        return "ClassMetadata{" +
                "className='" + className + '\'' +
                ", simpleName='" + simpleName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", constructorCount=" + constructors.size() +
                ", methodCount=" + methods.size() +
                ", fieldCount=" + fields.size() +
                ", annotationCount=" + annotations.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ClassMetadata that = (ClassMetadata) obj;
        return Objects.equals(className, that.className);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
    
    // 🔧 METHODS FOR COMPATIBILITY WITH io.warmup.framework.core.metadata.ClassMetadata
    
    /**
     * Check if class is interface (returns false by default)
     */
    public boolean isInterface() {
        return false; // Default implementation
    }
    
    /**
     * Check if class is final (returns false by default)
     */
    public boolean isFinal() {
        return false; // Default implementation
    }
    
    /**
     * Check if class is abstract (returns false by default)
     */
    public boolean isAbstract() {
        return false; // Default implementation
    }
}