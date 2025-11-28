package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Lazy;
import io.warmup.framework.annotation.PostConstruct;
import io.warmup.framework.annotation.PreDestroy;

@Component
public class UserService {

    @Inject
    @Lazy
    private HeavyService heavyService; // No se inicializa hasta que se use

    public User createUser() {
        return new User();
    }

    public void doSomething() {
        System.out.println("UserService haciendo algo...");
        // heavyService aún no se ha inicializado
    }

    public void useHeavyService() {
        System.out.println("Usando heavy service...");
        heavyService.process(); // Aquí se inicializa
    }

    @PostConstruct
    public void init() {
        System.out.println("Ejecutando Init en UserService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Ejecutando PreDestroy en UserService");
    }
}
