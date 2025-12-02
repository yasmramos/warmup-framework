package io.warmup.framework.aop;

import io.warmup.framework.core.AopHandler;
import io.warmup.framework.core.WarmupContainer;
// import io.warmup.framework.jit.asm.SimpleASMUtils; // MIGRATED to AsmCoreUtils
import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Creación de “subclases” con aspectos inyectados vía ASM puro. No usa Proxy,
 * ByteBuddy, cglib, etc.
 */
public final class AspectProxyASM {

    private static final Logger log = Logger.getLogger(AspectProxyASM.class.getName());

    private static final String HANDLER_FIELD = "__$aopHandler";
    private static final String TARGET_FIELD = "__$target";

    /* ---------- API pública ---------- */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> targetClass, WarmupContainer container) {
        AopHandler handler = (AopHandler) container.getAopHandler();
        if (!handler.shouldApplyAopToClass(targetClass)) {
            log.log(Level.FINE, "No aspects for {0} -> returning original", AsmCoreUtils.getSimpleClassName(targetClass));
            return target;
        }
        try {
            Class<? extends T> enhanced = generateSubclass(targetClass);
            T instance = newInstance(enhanced, target, handler);
            copyFields(target, instance);
            return instance;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot create ASM proxy for " + targetClass, e);
            return target;          // fallback
        }
    }

    /**
     * Método específico para crear proxies cuando el tipo no es conocido exactamente.
     */
    @SuppressWarnings("unchecked")
    public static Object createProxyForObject(Object target, Class<?> targetClass, WarmupContainer container) {
        AopHandler handler = (AopHandler) container.getAopHandler();
        if (!handler.shouldApplyAopToClass(targetClass)) {
            log.log(Level.FINE, "No aspects for {0} -> returning original", AsmCoreUtils.getSimpleClassName(targetClass));
            return target;
        }
        try {
            Class<?> enhanced = generateSubclassForObject(targetClass);
            Object instance = newInstanceForObject(enhanced, target, handler);
            copyFields(target, instance);
            return instance;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot create ASM proxy for " + targetClass, e);
            return target;          // fallback
        }
    }

    /* ---------- Generación ASM ---------- */
    private static <T> Class<? extends T> generateSubclass(Class<T> base) {
        String internalName = AsmCoreUtils.getClassName(base).replace('.', '/');
        
        // Generar nombre único para el proxy evitando conflictos con clases anidadas de Java
        // Usar una combinación de hash del nombre y timestamp para mayor uniqueness
        long timestamp = System.currentTimeMillis();
        String combinedHash = base.getName() + ":" + timestamp;
        String classHash = String.valueOf(Math.abs(combinedHash.hashCode())).substring(Math.max(0, String.valueOf(Math.abs(combinedHash.hashCode())).length() - 6));
        String subName;
        System.out.println("[PROXY ASM] Generating proxy for: " + base.getName() + " with hash: " + classHash + " at time: " + timestamp);
        if (internalName.contains("$")) {
            // Es clase interna - evitar la estructura $ completamente
            // Extraer el nombre simple y generar un nombre seguro
            String simpleInnerName = base.getSimpleName();
            String packagePath = internalName.substring(0, internalName.lastIndexOf('/') + 1);
            subName = packagePath + simpleInnerName + "_InnerClass_Proxy" + classHash;
        } else {
            // Es clase normal, usar el paquete y el nombre simple
            String packagePath = internalName.substring(0, internalName.lastIndexOf('/') + 1);
            String simpleName = base.getSimpleName();
            subName = packagePath + simpleName + "_Proxy" + classHash;
        }
        
        String[] interfaces = AsmCoreUtils.isInterface(base)
                ? new String[]{internalName}
                : null;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC | ACC_SUPER, subName, null,
                AsmCoreUtils.isInterface(base) ? "java/lang/Object" : internalName, interfaces);

        // campo estático: AopHandler
        cw.visitField(ACC_PRIVATE | ACC_STATIC, HANDLER_FIELD,
                AsmCoreUtils.getDescriptor(AopHandler.class), null, null).visitEnd();

        // campo de instancia: target original (por si se necesita)
        cw.visitField(ACC_PRIVATE, TARGET_FIELD,
                AsmCoreUtils.getDescriptor(Object.class), null, null).visitEnd();

        // constructor
        generateConstructor(cw, base, subName);

        // métodos
        List<Method> overridableMethods = collectOverridable(base);
        log.log(Level.INFO, "🔥 DEBUG: Generando proxy para {0} métodos de clase: {1}", new Object[]{overridableMethods.size(), base.getSimpleName()});
        for (Method m : overridableMethods) {
            log.log(Level.INFO, "   🔥 Generando método proxy: {0}", m.getName());
            generateMethod(cw, m, subName, internalName);
        }

        cw.visitEnd();
        byte[] bytecode = cw.toByteArray();

        // cargar
        ClassLoader parent = base.getClassLoader();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        ByteArrayClassLoader loader = new ByteArrayClassLoader(parent);
        return (Class<? extends T>) loader.defineClass(subName.replace('/', '.'), bytecode);
    }

    private static void generateConstructor(ClassWriter cw, Class<?> base, String subName) {
        // Constructor estándar para AOP: (Object target, AopHandler handler)
        Type[] argTypes = new Type[] {
            Type.getType(Object.class),
            Type.getType(AopHandler.class)
        };
        String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, argTypes);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", ctorDesc, null, null);
        mv.visitCode();
        
        // 1. Llamar al constructor de la superclase (sin argumentos)
        mv.visitVarInsn(ALOAD, 0);                       // this
        mv.visitMethodInsn(INVOKESPECIAL,
                AsmCoreUtils.isInterface(base) ? "java/lang/Object" : AsmCoreUtils.getInternalName(base),
                "<init>", "()V", false);
        
        // 2. Configurar el campo __$target
        mv.visitVarInsn(ALOAD, 0);                       // this
        mv.visitVarInsn(ALOAD, 1);                       // target
        mv.visitFieldInsn(PUTFIELD, subName, TARGET_FIELD, AsmCoreUtils.getDescriptor(Object.class));
        
        // 3. Configurar el campo estático __$aopHandler
        mv.visitVarInsn(ALOAD, 2);                       // handler
        mv.visitFieldInsn(PUTSTATIC, subName, HANDLER_FIELD, AsmCoreUtils.getDescriptor(AopHandler.class));
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void generateMethod(ClassWriter cw, Method m, String subName, String superName) {
        // ✅ ASM DIRECTO: Usar datos del método directamente
        String methodName = m.getName();
        String desc = AsmCoreUtils.getMethodDescriptor(m);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, desc, null, null);
        mv.visitCode();

        Class<?>[] params = AsmCoreUtils.getParameterTypes(m);
        
        // 1º STEP 1: Primero cargar TODOS los parámetros desde sus slots originales (1, 2, 3, etc.)
        // y copiarlos al array args. Los parámetros ocupan slots 1+ en la firma del método.
        
        // Crear el array args ANTES de cargar parámetros
        pushInt(mv, params.length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        int argsVar = 5; // Usar slot 5+ para evitar conflictos con parámetros
        mv.visitVarInsn(ASTORE, argsVar);
        
        // Cargar todos los parámetros desde sus slots originales y almacenarlos en el array
        for (int i = 0; i < params.length; i++) {
            Class<?> paramType = params[i];
            
            mv.visitVarInsn(ALOAD, argsVar);
            pushInt(mv, i);
            
            // Cargar parámetro desde su slot original (1 + i)
            // Los parámetros del método empiezan en slot 1 (después de 'this' en slot 0)
            if (paramType.isPrimitive()) {
                if (paramType == int.class || paramType == byte.class || 
                    paramType == char.class || paramType == short.class || paramType == boolean.class) {
                    mv.visitVarInsn(ILOAD, 1 + i); // Parámetros primitivos enteros
                } else if (paramType == long.class) {
                    mv.visitVarInsn(LLOAD, 1 + i);
                } else if (paramType == float.class) {
                    mv.visitVarInsn(FLOAD, 1 + i);
                } else if (paramType == double.class) {
                    mv.visitVarInsn(DLOAD, 1 + i);
                }
            } else {
                mv.visitVarInsn(ALOAD, 1 + i); // Tipos de referencia
            }
            
            boxIfNeeded(mv, paramType);
            mv.visitInsn(AASTORE);
        }

        // 2º STEP 2: AHORA sí, almacenar las variables locales en slots superiores
        // Ya no hay conflicto porque los parámetros ya están copiados al array
        
        // AopHandler handler = this.__$aopHandler;
        mv.visitFieldInsn(GETSTATIC, subName, HANDLER_FIELD, AsmCoreUtils.getDescriptor(AopHandler.class));
        int handlerVar = 1; // Ahora podemos usar slots 1-4 para variables locales
        mv.visitVarInsn(ASTORE, handlerVar);

        // Object target = this.__$target;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, subName, TARGET_FIELD, AsmCoreUtils.getDescriptor(Object.class));
        int targetVar = 2;
        mv.visitVarInsn(ASTORE, targetVar);

        // 3º Method realMethod = target.getClass().getDeclaredMethod(...)
        mv.visitVarInsn(ALOAD, targetVar);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitLdcInsn(methodName);
        pushInt(mv, params.length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        for (int i = 0; i < params.length; i++) {
            mv.visitInsn(DUP);
            pushInt(mv, i);
            pushClassLiteral(mv, params[i]);
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
        int methodVar = 3;
        mv.visitVarInsn(ASTORE, methodVar);

        // 4º handler.invokeWithAspects(target, method, args)
        log.log(Level.INFO, "🔥 DEBUG: Generando llamada AOP para método: {0}", m.getName());
        mv.visitVarInsn(ALOAD, handlerVar);
        mv.visitVarInsn(ALOAD, targetVar);
        mv.visitVarInsn(ALOAD, methodVar);
        mv.visitVarInsn(ALOAD, argsVar);
        mv.visitMethodInsn(INVOKEVIRTUAL,
                Type.getInternalName(AopHandler.class),
                "invokeWithAspects",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        log.log(Level.INFO, "   🔥 Llamada AOP generada para: {0}", m.getName());

        // 5º unbox / return
        Class<?> ret = AsmCoreUtils.getReturnType(m);
        if (ret == void.class) {
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
        } else {
            if (ret == java.util.concurrent.CompletableFuture.class) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(java.util.concurrent.CompletableFuture.class));
                mv.visitInsn(ARETURN);
            } else {
                unboxIfNeeded(mv, ret);
                mv.visitInsn(getReturnInsn(ret));
            }
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /* ---------- helpers ASM ---------- */
    private static void pushInt(MethodVisitor mv, int i) {
        if (i >= -1 && i <= 5) {
            mv.visitInsn(ICONST_0 + i);
        } else if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, i);
        } else {
            mv.visitIntInsn(SIPUSH, i);
        }
    }

    private static void boxIfNeeded(MethodVisitor mv, Class<?> c) {
        if (!c.isPrimitive()) {
            return;
        }
        String box = AsmCoreUtils.getInternalName(boxClass(c));
        mv.visitMethodInsn(INVOKESTATIC, box, "valueOf", "(" + AsmCoreUtils.getDescriptor(c) + ")L" + box + ";", false);
    }

    private static void unboxIfNeeded(MethodVisitor mv, Class<?> c) {
        if (!c.isPrimitive()) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(c));
            return;
        }
        String box = AsmCoreUtils.getInternalName(boxClass(c));
        String unbox = AsmCoreUtils.getClassName(c) + "Value";
        String desc = "()" + AsmCoreUtils.getDescriptor(c);
        mv.visitTypeInsn(CHECKCAST, box);
        mv.visitMethodInsn(INVOKEVIRTUAL, box, unbox, desc, false);
    }

    private static int getReturnInsn(Class<?> c) {
        if (c == void.class) {
            return RETURN;
        }
        if (c == float.class) {
            return FRETURN;
        }
        if (c == double.class) {
            return DRETURN;
        }
        if (c == long.class) {
            return LRETURN;
        }
        if (c.isPrimitive()) {
            return IRETURN;
        }
        return ARETURN;
    }

    private static final Map<Class<?>, Class<?>> PRIM_TO_BOX = new HashMap<>();

    static {
        PRIM_TO_BOX.put(boolean.class, Boolean.class);
        PRIM_TO_BOX.put(byte.class, Byte.class);
        PRIM_TO_BOX.put(short.class, Short.class);
        PRIM_TO_BOX.put(int.class, Integer.class);
        PRIM_TO_BOX.put(long.class, Long.class);
        PRIM_TO_BOX.put(float.class, Float.class);
        PRIM_TO_BOX.put(double.class, Double.class);
        PRIM_TO_BOX.put(char.class, Character.class);
    }

    private static Class<?> boxClass(Class<?> c) {
        return PRIM_TO_BOX.get(c);
    }

    private static void pushClassLiteral(MethodVisitor mv, Class<?> c) {
        if (c == boolean.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
        } else if (c == byte.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
        } else if (c == short.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
        } else if (c == int.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        } else if (c == long.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
        } else if (c == float.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
        } else if (c == double.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
        } else if (c == char.class) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
        } else {
            // Manejo de value classes y tipos de referencia
            String className = AsmCoreUtils.getClassName(c);
            mv.visitLdcInsn(className);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", 
                             "(Ljava/lang/String;)Ljava/lang/Class;", false);
        }
    }

    /* ---------- reflexión / util ---------- */
    private static List<Method> collectOverridable(Class<?> c) {
        List<Method> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        
        // ✅ ASM DIRECTO: Obtener métodos usando ASM sin conversión
        io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo[] asmMethods = AsmCoreUtils.getDeclaredMethods(c.getName());
        log.log(Level.INFO, "🔥 DEBUG: ASM encontró {0} métodos para clase: {1}", new Object[]{asmMethods.length, c.getSimpleName()});
        
        for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo asmMethod : asmMethods) {
            log.log(Level.INFO, "   🔥 ASM método: {0} - {1}", new Object[]{asmMethod.name, asmMethod.descriptor});
            // Verificar modifiers usando flags de ASM
            if (asmMethod.isFinal || asmMethod.isStatic) {
                log.log(Level.INFO, "      🔥 Filtrado por ser final o static");
                continue;
            }
            
            // Crear firma del método usando datos ASM
            String signature = asmMethod.name + asmMethod.descriptor;
            if (seen.add(signature)) {
                // Convertir a Method temporal para compatibilidad
                try {
                    Class<?>[] paramTypes = new Class<?>[asmMethod.parameterTypes.length];
                    for (int i = 0; i < asmMethod.parameterTypes.length; i++) {
                        paramTypes[i] = AsmCoreUtils.getClassFromDescriptor(asmMethod.parameterTypes[i]);
                    }
                    log.log(Level.INFO, "      🔥 Intentando getDeclaredMethod: {0} en clase: {1}", new Object[]{asmMethod.name, c.getName()});
                    Method method = c.getDeclaredMethod(asmMethod.name, paramTypes);
                    log.log(Level.INFO, "      ✅ Método encontrado y agregado: {0}", method.getName());
                    list.add(method);
                } catch (NoSuchMethodException e) {
                    log.log(Level.WARNING, "      ❌ getDeclaredMethod falló, intentando getMethod: {0}", asmMethod.name);
                    try {
                        // Para clases internas, intentar con getMethod
                        Class<?>[] paramTypes = new Class<?>[asmMethod.parameterTypes.length];
                        for (int i = 0; i < asmMethod.parameterTypes.length; i++) {
                            paramTypes[i] = AsmCoreUtils.getClassFromDescriptor(asmMethod.parameterTypes[i]);
                        }
                        Method method = c.getMethod(asmMethod.name, paramTypes);
                        log.log(Level.INFO, "      ✅ Método encontrado con getMethod y agregado: {0}", method.getName());
                        list.add(method);
                    } catch (NoSuchMethodException e2) {
                        log.log(Level.WARNING, "      ❌ Ambos métodos fallaron para: {0}", asmMethod.name);
                        // Continuar si no se puede encontrar el método
                        continue;
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "      ❌ Exception para: {0} - {1}", new Object[]{asmMethod.name, e.getMessage()});
                    // Continuar si no se puede encontrar el método
                    continue;
                }
            } else {
                log.log(Level.INFO, "      🔥 Firma ya vista, omitiendo: {0}", signature);
            }
        }
        return list;
    }

    private static <T> T newInstance(Class<? extends T> clazz, T target, AopHandler handler)
            throws Exception {
        // ✅ FIX: Usar el mismo class loader que cargó la clase generada
        T inst = createInstanceWithCorrectClassLoader(clazz);
        setStaticField(clazz, HANDLER_FIELD, handler);
        setField(inst, TARGET_FIELD, target);
        return inst;
    }
    
    /**
     * ✅ FIX: Crear instancia usando el class loader correcto que cargó la clase generada
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInstanceWithCorrectClassLoader(Class<? extends T> clazz) {
        try {
            // Usar el class loader de la clase para encontrar el constructor y crear la instancia
            java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(Object.class, AopHandler.class);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(null, null); // target y handler se establecen después
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance with correct class loader: " + clazz.getName(), e);
        }
    }

    private static void setField(Object instance, String fieldName, Object value) {
        try {
            AsmCoreUtils.setFieldValue(instance, fieldName, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            // ✅ FIX: Para campos estáticos, usar reflexión directa en lugar de AsmCoreUtils
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value); // null para campos estáticos (se establece en la clase, no en una instancia)
        } catch (Exception e) {
            throw new RuntimeException("Failed to set static field: " + fieldName + " on class: " + clazz.getName(), e);
        }
    }

    private static void copyFields(Object src, Object dst) throws Exception {
        Class<?> c = src.getClass();
        while (c != Object.class) {
            for (java.lang.reflect.Field f : AsmCoreUtils.getDeclaredFieldsReflect(c)) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                Object value = f.get(src);
                AsmCoreUtils.setFieldValue(dst, f.getName(), value);
            }
            c = AsmCoreUtils.getSuperclass(c);
        }
    }

    /**
     * Método auxiliar para generar subclases sin restricciones de tipos genéricos.
     */
    @SuppressWarnings("unchecked")
    private static Class<?> generateSubclassForObject(Class<?> base) {
        String internalName = AsmCoreUtils.getClassName(base).replace('.', '/');
        
        // Generar nombre único para el proxy evitando conflictos con clases anidadas de Java
        // Usar una combinación de hash del nombre y timestamp para mayor uniqueness
        long timestamp = System.currentTimeMillis();
        String combinedHash = base.getName() + ":" + timestamp;
        String classHash = String.valueOf(Math.abs(combinedHash.hashCode())).substring(Math.max(0, String.valueOf(Math.abs(combinedHash.hashCode())).length() - 6));
        String subName;
        System.out.println("[PROXY ASM] Generating proxy for: " + base.getName() + " with hash: " + classHash + " at time: " + timestamp);
        if (internalName.contains("$")) {
            // Es clase interna - evitar la estructura $ completamente
            // Extraer el nombre simple y generar un nombre seguro
            String simpleInnerName = base.getSimpleName();
            String packagePath = internalName.substring(0, internalName.lastIndexOf('/') + 1);
            subName = packagePath + simpleInnerName + "_InnerClass_Proxy" + classHash;
        } else {
            // Es clase normal, usar el paquete y el nombre simple
            String packagePath = internalName.substring(0, internalName.lastIndexOf('/') + 1);
            String simpleName = base.getSimpleName();
            subName = packagePath + simpleName + "_Proxy" + classHash;
        }
        
        String[] interfaces = AsmCoreUtils.isInterface(base)
                ? new String[]{internalName}
                : null;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC | ACC_SUPER, subName, null,
                AsmCoreUtils.isInterface(base) ? "java/lang/Object" : internalName, interfaces);

        // campo estático: AopHandler
        cw.visitField(ACC_PRIVATE | ACC_STATIC, HANDLER_FIELD,
                AsmCoreUtils.getDescriptor(AopHandler.class), null, null).visitEnd();

        // campo de instancia: target original (por si se necesita)
        cw.visitField(ACC_PRIVATE, TARGET_FIELD,
                AsmCoreUtils.getDescriptor(Object.class), null, null).visitEnd();

        // Constructor
        generateConstructor(cw, base, subName);

        // Métodos
        // ✅ ASM DIRECTO: Obtener métodos usando ASM
        io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo[] asmMethods = 
            AsmCoreUtils.getDeclaredMethods(base.getName());
        
        for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo asmMethod : asmMethods) {
            if ((asmMethod.access & Modifier.ABSTRACT) != 0) {
                continue; // Skip abstract methods
            }
            // Convertir AsmMethodInfo a Method temporal para generateMethod
            Method method = AsmCoreUtils.asmMethodToReflectMethod(asmMethod, base);
            generateMethod(cw, method, subName, internalName);
        }

        byte[] classBytes = cw.toByteArray();
        // Convert internal name (with /) to fully qualified name (with .) for defineClass
        String fullyQualifiedName = subName.replace('/', '.');
        ClassLoader parent = base.getClassLoader();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        ByteArrayClassLoader loader = new ByteArrayClassLoader(parent);
        return (Class<?>) loader.defineClass(fullyQualifiedName, classBytes);
    }

    /**
     * Método auxiliar para crear instancias sin restricciones de tipos genéricos.
     */
    @SuppressWarnings("unchecked")
    private static Object newInstanceForObject(Class<?> clazz, Object target, AopHandler handler) {
        try {
            // ✅ FIX: Usar el class loader correcto que cargó la clase generada
            Constructor<?> ctor = clazz.getDeclaredConstructor(Object.class, AopHandler.class);
            return ctor.newInstance(target, handler);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot create instance for " + clazz + " - ClassLoader: " + clazz.getClassLoader(), e);
            return target;
        }
    }

    /**
     * ✅ Crea un ClassLoader apropiado para la clase que se va a proxificar
     * Especialmente importante para clases estáticas anidadas
     */
    private static ByteArrayClassLoader createClassLoaderForProxy(Class<?> base) {
        // Para clases estáticas anidadas (que contienen $), usar el class loader de la clase enclosing
        if (base.getName().contains("$")) {
            try {
                // Obtener la clase enclosing y usar su class loader
                Class<?> enclosingClass = base.getDeclaringClass();
                ClassLoader enclosingClassLoader = enclosingClass.getClassLoader();
                log.log(Level.FINE, "Usando class loader de clase enclosing para {0}: {1}", 
                    new Object[]{base.getSimpleName(), enclosingClassLoader});
                return new ByteArrayClassLoader(enclosingClassLoader);
            } catch (Exception e) {
                log.log(Level.WARNING, "No se pudo obtener class loader de clase enclosing para {0}, usando class loader de la clase base", base.getSimpleName());
                // Fallback al class loader original
            }
        }
        
        // Para clases normales, usar el class loader de la clase base
        log.log(Level.FINE, "Usando class loader de clase base para {0}: {1}", 
            new Object[]{base.getSimpleName(), base.getClassLoader()});
        return new ByteArrayClassLoader(base.getClassLoader());
    }

    /* ---------- ClassLoader simple ---------- */
    private static class ByteArrayClassLoader extends ClassLoader {

        ByteArrayClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length, (ProtectionDomain) null);
        }
    }
}
