package io.warmup.examples.config;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.core.BasicPlatformTransactionManager;
import io.warmup.framework.core.PlatformTransactionManager;
import io.warmup.framework.core.TransactionManager;
import io.warmup.framework.core.TransactionAspect;
import io.warmup.examples.services.SimpleAuditService;
import io.warmup.examples.services.SimpleTransactionalUserService;
import java.util.logging.Logger;

/**
 * Configuration class for transactional services.
 * 
 * <p>
 * This configuration demonstrates:
 * <ul>
 * <li>Transaction manager setup</li>
 * <li>Transactional service configuration</li>
 * <li>Aspect registration for transaction management</li>
 * <li>Service dependency injection with transactions</li>
 * </ul>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
@Configuration
public class TransactionalConfiguration {

    private static final Logger log = Logger.getLogger(TransactionalConfiguration.class.getName());

    /**
     * Configures the default transaction manager.
     */
    @Bean
    @Singleton
    public PlatformTransactionManager defaultTransactionManager() {
        log.info("Configuring default transaction manager");
        return new BasicPlatformTransactionManager("defaultTransactionManager");
    }

    /**
     * Configures the main transaction manager.
     */
    @Bean
    @Singleton
    public TransactionManager transactionManager(PlatformTransactionManager defaultTransactionManager) {
        log.info("Configuring main transaction manager");
        TransactionManager transactionManager = new TransactionManager(defaultTransactionManager);
        
        // Register additional transaction managers if needed
        // transactionManager.registerTransactionManager("database1", databaseTransactionManager);
        // transactionManager.registerTransactionManager("database2", anotherDatabaseTransactionManager);
        
        return transactionManager;
    }

    /**
     * Configures the transaction aspect for AOP support.
     */
    @Bean
    @Singleton
    public TransactionAspect transactionAspect(TransactionManager transactionManager) {
        log.info("Configuring transaction aspect");
        return new TransactionAspect(transactionManager);
    }

    /**
     * Configures the audit service.
     */
    @Bean
    @Singleton
    public SimpleAuditService auditService() {
        log.info("Configuring audit service");
        return new SimpleAuditService();
    }

    /**
     * Configures the transactional user service.
     */
    @Bean
    @Singleton
    public SimpleTransactionalUserService transactionalUserService(SimpleAuditService auditService) {
        log.info("Configuring transactional user service");
        return new SimpleTransactionalUserService(auditService);
    }
}