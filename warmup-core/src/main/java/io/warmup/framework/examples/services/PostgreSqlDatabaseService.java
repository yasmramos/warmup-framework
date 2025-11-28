package io.warmup.framework.examples.services;

public class PostgreSqlDatabaseService implements DatabaseService {
    @Override
    public void connect() {
        System.out.println("🐘 PostgreSQL: Connecting to PostgreSQL database");
    }
    
    @Override
    public void disconnect() {
        System.out.println("🐘 PostgreSQL: Disconnecting from database");
    }
    
    @Override
    public String getDatabaseInfo() {
        return "PostgreSQL Database Service - Advanced relational database";
    }
}