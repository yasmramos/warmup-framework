package io.warmup.framework.core.plugin;

public interface Plugin {

    String name();

    String version();

    int order();

    void onRegister(PluginContext ctx);

    void onStart(Object container);

    void onStop();
}
