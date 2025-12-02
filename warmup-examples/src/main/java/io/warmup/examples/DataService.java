package io.warmup.examples;

import io.warmup.framework.annotation.PreDestroy;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.PostConstruct;
import io.warmup.framework.annotation.Named;
import io.warmup.framework.annotation.Inject;

@Component
public class DataService {

    private final String appName;
    private final int timeout;
    private final java.sql.Connection connection;

    @Inject
    public DataService(@Named("appName") String appName,
            @Named("timeout") int timeout,
            @Named("primary") java.sql.Connection connection) {
        this.appName = appName;
        this.timeout = timeout;
        this.connection = connection;
    }

    @PostConstruct
    void init() {
        System.out.println("✅ DataService inicializado");
        System.out.println("   App name: " + appName);
        System.out.println("   Timeout: " + timeout + " s");
        System.out.println("   Connection: " + connection);
    }

    @PreDestroy
    void close() {
        System.out.println("🛑 DataService cerrando");
    }
}
