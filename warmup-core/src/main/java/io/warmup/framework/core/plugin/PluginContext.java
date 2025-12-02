package io.warmup.framework.core.plugin;

public interface PluginContext {

    void addExtension(Extension ext);

    <T> T getBean(Class<?> type);
    
    String getProperty(String key, String defaultValue);
}
