import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

// Test rápido para verificar la lógica de la corrección
public class test_methodhandle_fix {
    
    // Simular el problema original
    public static void main(String[] args) throws Throwable {
        
        // 1. Crear MethodHandle UNBOUND
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        
        // Método que simula: handle(TestEvent event)
        java.lang.reflect.Method method = TestListener.class.getMethod("handle", TestEvent.class);
        MethodHandle methodHandle = lookup.unreflect(method);
        
        System.out.println("MethodHandle type: " + methodHandle.type());
        System.out.println("MethodHandle type parameter count: " + methodHandle.type().parameterCount());
        
        // 2. Simular lo que haría el código CORREGIDO
        TestEvent event = new TestEvent();
        TestListener listener = new TestListener();
        
        // Args que recibiría adaptArguments([event])
        Object[] originalArgs = new Object[]{event};
        
        // Lo que devuelve adaptArguments() CORREGIDO: [event]
        Object[] adaptedArgs = originalArgs;
        
        // Lo que debería recibir el MethodHandle UNBOUND: [listener, event]
        Object[] allArgs = new Object[adaptedArgs.length + 1];
        allArgs[0] = listener;
        System.arraycopy(adaptedArgs, 0, allArgs, 1, adaptedArgs.length);
        
        System.out.println("Array que va al MethodHandle: [" + allArgs[0].getClass().getSimpleName() + ", " + allArgs[1].getClass().getSimpleName() + "]");
        
        // Test de invocación
        try {
            methodHandle.invokeWithArguments(allArgs);
            System.out.println("✅ INVOCACIÓN EXITOSA");
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Clases de test
    static class TestEvent {
        public String data = "test";
    }
    
    static class TestListener {
        public void handle(TestEvent event) {
            System.out.println("✅ handle() llamado con: " + event.data);
        }
    }
}