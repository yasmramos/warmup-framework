package io.warmup.examples.module;

import io.warmup.framework.annotation.Provides;
import io.warmup.framework.annotation.Factory;
import io.warmup.framework.annotation.Value;
import io.warmup.framework.annotation.Named;
import io.warmup.framework.module.AbstractModule;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // nada – todo vía @Provides
    }

    /* ---------- @Provides methods ---------- */
    @Provides
    @Named("appName")
    public String appName(@Value("${app.name:MyApp}") String name) {
        return name;
    }

    @Provides
    @Named("timeout")
    public int timeout(@Value("${db.timeout:30}") int seconds) {
        return seconds;
    }

    /* ---------- @Factory class ---------- */
    @Factory
    public static class ConnectionFactory {

        @Provides
        @Named("primary")
        public Connection createConnection(@Value("${db.url:jdbc:h2:mem:test}") String url,
                @Value("${db.user:sa}") String user) {
            try {
                return DriverManager.getConnection(url, user, "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
