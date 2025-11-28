package io.warmup.framework.examples.services;

public interface DatabaseService {
    void connect();
    void disconnect();
    String getDatabaseInfo();
}