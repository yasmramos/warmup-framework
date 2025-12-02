package io.warmup.framework.module;

import io.warmup.framework.core.WarmupContainer;
import java.util.*;
import java.util.logging.Logger;

public final class BindingValidator {

    private static final Logger log = Logger.getLogger(BindingValidator.class.getName());

    /* ---------- API pública ---------- */
    public static void validate(WarmupContainer container, List<Module> modules) {
        log.info("Validando bindings...");
        Map<Key, Target> bindings = collectBindings(container, modules);
        checkUnresolvedBindings(bindings);
        checkCircularDependencies(bindings);
        checkDuplicateBindings(bindings);
        checkInterfacesWithoutImpl(bindings);
        log.info("✅ Bindings válidos");
    }

    /* ---------- estructuras internas ---------- */
    private static class Key {

        final Class<?> type;
        final String name;

        Key(Class<?> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(type, key.type) && Objects.equals(name, key.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }
    }

    private static class Target {

        final Class<?> impl;
        final boolean singleton;

        Target(Class<?> impl, boolean singleton) {
            this.impl = impl;
            this.singleton = singleton;
        }
    }

    private static Map<Key, Target> collectBindings(WarmupContainer container, List<Module> modules) {
        Map<Key, Target> map = new HashMap<>();
        for (Module m : modules) {
            if (!m.isEnabled()) {
                continue;
            }
            m.configure(new ValidationBinder(map, container));
        }
        return map;
    }

    /* ---------- 1. bindings sin target ---------- */
    private static void checkUnresolvedBindings(Map<Key, Target> bindings) {
        for (Map.Entry<Key, Target> e : bindings.entrySet()) {
            if (e.getValue().impl == null) {
                throw new IllegalStateException("Binding sin target: " + e.getKey().type.getName()
                        + (e.getKey().name != null ? " @" + e.getKey().name : ""));
            }
        }
    }

    /* ---------- 2. ciclos en dependencias ---------- */
    private static void checkCircularDependencies(Map<Key, Target> bindings) {
        Set<Key> visited = new HashSet<>();
        Set<Key> visiting = new HashSet<>();

        for (Key key : bindings.keySet()) {
            if (!visited.contains(key)) {
                checkForCycles(key, bindings, visited, visiting);
            }
        }
    }

    private static void checkForCycles(Key key, Map<Key, Target> bindings, Set<Key> visited, Set<Key> visiting) {
        if (visiting.contains(key)) {
            throw new IllegalStateException("Ciclo detectado en bindings: " + key.type.getName()
                    + (key.name != null ? " @" + key.name : ""));
        }

        if (visited.contains(key)) {
            return;
        }

        visiting.add(key);
        Target target = bindings.get(key);
        if (target != null && target.impl != null) {
            // Solo verificar si la implementación también está en los bindings
            Key implKey = new Key(target.impl, null);
            if (bindings.containsKey(implKey)) {
                checkForCycles(implKey, bindings, visited, visiting);
            }
        }
        visiting.remove(key);
        visited.add(key);
    }

    /* ---------- 3. duplicados ---------- */
    private static void checkDuplicateBindings(Map<Key, Target> bindings) {
        Set<Key> seen = new HashSet<>();
        for (Key k : bindings.keySet()) {
            if (!seen.add(k)) {
                throw new IllegalStateException("Binding duplicado: " + k.type.getName()
                        + (k.name != null ? " @" + k.name : ""));
            }
        }
    }

    /* ---------- 4. interfaces sin impl ---------- */
    private static void checkInterfacesWithoutImpl(Map<Key, Target> bindings) {
        for (Map.Entry<Key, Target> e : bindings.entrySet()) {
            Class<?> type = e.getKey().type;
            if (type.isInterface() && e.getValue().impl == null) {
                throw new IllegalStateException("Interface sin implementación: " + type.getName()
                        + (e.getKey().name != null ? " @" + e.getKey().name : ""));
            }
        }
    }

    /* ---------- 5. requireBinding ---------- */
    public static void requireBinding(WarmupContainer container, Class<?> type, String name) {
        if (!container.hasBinding(type, name)) {
            String key = name == null ? type.getName() : type.getName() + ":" + name;
            throw new IllegalStateException("requireBinding falló: " + key);
        }
    }

    /* ---------- binder de validación (solo recolecta) ---------- */
    private static final class ValidationBinder implements Binder {

        private final Map<Key, Target> map;
        private final WarmupContainer container;

        ValidationBinder(Map<Key, Target> map, WarmupContainer container) {
            this.map = map;
            this.container = container;
        }

        @Override
        public WarmupContainer container() {
            return this.container;
        }

        @Override
        public <T> ScopedBindingBuilder<T> bind(Class<T> type) {
            return bind(type, null);
        }

        @Override
        public <T> ScopedBindingBuilder<T> bind(Class<T> type, String name) {
            Key key = new Key(type, name);
            return new ScopedBindingBuilder<T>() {
                private Class<? extends T> impl;
                private T instance;
                private boolean singleton = false;

                @Override
                public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
                    // Para validación, podemos tratar el provider como una implementación
                    // o simplemente ignorarlo ya que es complejo validar providers
                    return this;
                }

                @Override
                public ScopedBindingBuilder<T> annotatedWith(String qualifier) {
                    return bind(type, qualifier);
                }

                @Override
                public ScopedBindingBuilder<T> to(Class<? extends T> implementation) {
                    this.impl = implementation;
                    return this;
                }

                @Override
                public ScopedBindingBuilder<T> toInstance(T instance) {
                    this.instance = instance;
                    return this;
                }

                @Override
                public ScopedBindingBuilder<T> asSingleton() {
                    this.singleton = true;
                    return this;
                }

                @Override
                public void done() {
                    if (instance != null) {
                        map.put(key, new Target(instance.getClass(), singleton));
                    } else if (impl != null) {
                        map.put(key, new Target(impl, singleton));
                    } else {
                        map.put(key, new Target(null, singleton));
                    }
                }
            };
        }

        @Override
        public void install(Module module) {
            if (!module.isEnabled()) {
                return;
            }
            module.configure(this);
        }

        @Override
        public <T> void requireBinding(Class<T> type) {
            BindingValidator.requireBinding(container, type, null);
        }

        @Override
        public <T> void requireBinding(Class<T> type, String name) {
            BindingValidator.requireBinding(container, type, name);
        }
    }
}
