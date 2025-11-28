package io.warmup.framework.examples.services;

public class RelaxedSecurityService {
    public void performSecurityCheck() {
        System.out.println("🔐 Relaxed Security: Basic security check performed");
    }
    
    public String getInfo() {
        return "Relaxed Security Service - Minimal security restrictions";
    }
}