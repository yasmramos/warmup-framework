package io.warmup.examples;

import io.warmup.framework.annotation.Component;

@Component
public class ServiceImpl implements Service {

    @Override
    public String process(String input) {
        return "Processed: " + input;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
