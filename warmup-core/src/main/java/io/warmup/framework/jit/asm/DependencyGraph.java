package io.warmup.framework.jit.asm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyGraph {

    private final Map<Class<?>, DependencyNode> nodes = new ConcurrentHashMap<>();

    public static class DependencyNode {

        private final Class<?> clazz;
        private final List<DependencyNode> dependencies = new ArrayList<>();
        private final List<DependencyNode> dependents = new ArrayList<>();

        public DependencyNode(Class<?> clazz) {
            this.clazz = clazz;
        }

        public void addDependency(DependencyNode node) {
            dependencies.add(node);
            node.dependents.add(this);
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public List<DependencyNode> getDependencies() {
            return dependencies;
        }
    }

    public DependencyNode getOrCreateNode(Class<?> clazz) {
        return nodes.computeIfAbsent(clazz, DependencyNode::new);
    }

    public List<Class<?>> getResolutionOrder() {
        // Orden topológico simple para demo
        return new ArrayList<>(nodes.keySet());
    }
}
