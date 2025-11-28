package io.warmup.framework.examples.services;

public class MySqlDatabaseService implements DatabaseService {
    @Override
    public void connect() {
        System.out.println("🐬 MySQL: Connecting to MySQL database");
    }
    
    @Override
    public void disconnect() {
        System.out.println("🐬 MySQL: Disconnecting from database");
    }
    
    @Override
    public String getDatabaseInfo() {
        return "MySQL Database Service - Popular relational database";
    }
}