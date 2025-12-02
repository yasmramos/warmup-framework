package io.warmup.framework.jit.asm;

/**
 * ✅ INTERFACES PARA CLASES GENERADAS DINÁMICAMENTE
 * 
 * Estas interfaces son implementadas por las clases que se generan dinámicamente
 * para reemplazar las operaciones de reflexión.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class GeneratedClassInterfaces {
    
    /**
     * Interface para clases que invocan métodos sin reflexión
     */
    public interface MethodInvokerClass {
        /**
         * Invoca un método en el target especificado
         * @param target El objeto objetivo
         * @param args Argumentos del método (array de objetos para flexiblidad)
         * @return El resultado de la invocación del método (boxed para objetos, null para void)
         */
        Object invoke(Object target, Object[] args);
    }
    
    /**
     * Interface para clases que acceden a campos sin reflexión
     */
    public interface FieldAccessorClass {
        /**
         * Obtiene el valor de un campo del objeto target
         * @param target El objeto objetivo
         * @return El valor del campo (boxed para primitivos)
         */
        Object getValue(Object target);
        
        /**
         * Establece el valor de un campo en el objeto target
         * @param target El objeto objetivo
         * @param value El nuevo valor del campo (boxed para primitivos)
         */
        void setValue(Object target, Object value);
    }
    
    /**
     * Interface para clases que copian campos entre objetos sin reflexión
     */
    public interface FieldCopierClass {
        /**
         * Copia todos los campos de instancia desde source hacia destination
         * @param source El objeto fuente
         * @param destination El objeto destino
         */
        void copyFields(Object source, Object destination);
    }
}