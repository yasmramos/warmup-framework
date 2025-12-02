package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Named;

@Component
@Named("emailNotifier")  // ✅ Agregar nombre
public class EmailNotifier implements Notifier {

    @Override
    public void send(String message) {
        System.out.println("[EMAIL] Enviando: " + message);
    }
}
