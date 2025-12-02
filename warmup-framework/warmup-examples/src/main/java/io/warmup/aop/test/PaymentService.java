package io.warmup.aop.test;

import io.warmup.framework.annotation.Timed;

public class PaymentService {

    @Timed   // <-- para probar @annotation(..)
    public String pay(String card, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        return "Payed " + amount + " with " + card;
    }

    public int slowMethod() throws InterruptedException {
        Thread.sleep(200);
        return 42;
    }
}