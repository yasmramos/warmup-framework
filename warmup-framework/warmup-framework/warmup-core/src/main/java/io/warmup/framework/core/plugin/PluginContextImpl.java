package io.warmup.framework.core.plugin;

public final class PluginContextImpl implements PluginContext {

    private final ExtensionRegistry registry;

    public PluginContextImpl(ExtensionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void addExtension(Extension ext) {
        registry.addExtension(ext);
    }

    @Override
    public <T> T getBean(Class<?> type) {
        return null; //WarmupContainer.getGlobal().get(type);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

}
