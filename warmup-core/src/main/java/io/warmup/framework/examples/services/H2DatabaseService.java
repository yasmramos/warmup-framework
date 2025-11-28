package io.warmup.framework.examples.services;

public class H2DatabaseService implements DatabaseService {
    @Override
    public void connect() {
        System.out.println("🗄️ H2: Connecting to H2 in-memory database");
    }
    
    @Override
    public void disconnect() {
        System.out.println("🗄️ H2: Disconnecting from database");
    }
    
    @Override
    public String getDatabaseInfo() {
        return "H2 Database Service - In-memory database for development";
    }
}