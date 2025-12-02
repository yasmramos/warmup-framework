package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Profile;

@Component
@Profile("dev") // Solo se registrará si el perfil "dev" está activo
public class DevDatabaseService implements DatabaseService {

    @Override
    public String getType() {
        return "Development Database";
    }

    @Override
    public void connect() {
        System.out.println("Conectando a la base de datos de desarrollo...");
    }
}
