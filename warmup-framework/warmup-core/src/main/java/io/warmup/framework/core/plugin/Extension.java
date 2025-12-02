package io.warmup.framework.core.plugin;

public interface Extension {

    String extensionPoint();

    Class<?> type();
}
