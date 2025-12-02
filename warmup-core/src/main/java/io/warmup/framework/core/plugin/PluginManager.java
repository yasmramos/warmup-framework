package io.warmup.framework.core.plugin;

import io.warmup.framework.core.WarmupContainer;
import java.util.List;

public final class PluginManager {

    private final List<Plugin> plugins;
    private final PluginContext context;
    private final ExtensionRegistry registry;

    public PluginManager() {
        this.plugins = new PluginLoader().load();
        this.registry = new ExtensionRegistry();
        this.context = new PluginContextImpl(this.registry);
    }

    public void register() {
        for (Plugin plugin : plugins) {
            plugin.onRegister(context);
        }
    }

    public void start(WarmupContainer container) {
        for (Plugin plugin : plugins) {
            plugin.onStart(container);
        }
    }

    public void stop() {
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugins.get(i).onStop();
        }
    }

    public <T> List<T> getExtensions(String point, Class<T> type) {
        return registry.getExtensions(point, type);
    }

}
