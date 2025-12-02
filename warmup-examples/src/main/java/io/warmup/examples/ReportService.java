package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Lazy;

@Component
public class ReportService {

    private final HeavyService heavyService;

    @Inject
    public ReportService(@Lazy HeavyService heavyService) {
        this.heavyService = heavyService; // Proxy lazy
    }
}
