package io.warmup.examples;

import io.warmup.framework.annotation.Component;


@Component
public class SmsNotifier implements Notifier {

    @Override
    public void send(String message) {
        System.out.println("[SMS] Enviando: " + message);
    }
}
