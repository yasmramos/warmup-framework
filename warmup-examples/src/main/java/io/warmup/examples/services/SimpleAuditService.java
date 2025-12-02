package io.warmup.examples.services;

import java.util.logging.Logger;

/**
 * Simple example audit service for transactional examples.
 * This is a simplified version for demonstration purposes.
 */
public class SimpleAuditService {
    
    private static final Logger log = Logger.getLogger(SimpleAuditService.class.getName());
    
    public void logOperation(String operation) {
        log.info("Audit: " + operation);
    }
    
    public void logTransaction(String transaction) {
        log.info("Transaction audit: " + transaction);
    }
}