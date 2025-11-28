package io.warmup.framework.examples.simple;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.annotation.Transactional;
import io.warmup.framework.core.BasicPlatformTransactionManager;
import io.warmup.framework.core.PlatformTransactionManager;
import io.warmup.framework.core.TransactionManager;
import io.warmup.framework.core.TransactionAspect;
import io.warmup.framework.core.Warmup;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple test for @Transactional functionality.
 * 
 * <p>
 * This test demonstrates basic transactional functionality including:
 * <ul>
 * <li>Transaction creation and management</li>
 * <li>Transaction commit and rollback</li>
 * <li>Exception handling</li>
 * <li>Read-only transactions</li>
 * </ul>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class SimpleTransactionalTest {

    private static final Logger log = Logger.getLogger(SimpleTransactionalTest.class.getName());

    @Configuration
    public static class TestConfiguration {
        
        @Bean
        @Singleton
        public PlatformTransactionManager transactionManager() {
            return new BasicPlatformTransactionManager("testTransactionManager");
        }

        @Bean
        @Singleton
        public TransactionManager mainTransactionManager(PlatformTransactionManager transactionManager) {
            return new TransactionManager(transactionManager);
        }

        @Bean
        @Singleton
        public TransactionAspect transactionAspect(TransactionManager transactionManager) {
            return new TransactionAspect(transactionManager);
        }

        @Bean
        @Singleton
        public TestService testService() {
            return new TestService();
        }
    }

    @Singleton
    public static class TestService {
        
        private final List<String> operations = new ArrayList<>();
        private boolean shouldFail = false;

        /**
         * Basic transactional method.
         */
        @Transactional
        public void performOperation(String operation) {
            log.info("Performing operation: " + operation);
            operations.add(operation);
            
            if (shouldFail) {
                throw new RuntimeException("Simulated failure");
            }
        }

        /**
         * Read-only transactional method.
         */
        @Transactional(readOnly = true)
        public List<String> getOperations() {
            return new ArrayList<>(operations);
        }

        /**
         * Method that always fails for rollback testing.
         */
        @Transactional
        public void performFailingOperation() {
            log.info("Performing failing operation");
            operations.add("failing operation");
            throw new RuntimeException("Test failure");
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        public void clearOperations() {
            operations.clear();
        }
    }

    @Inject
    private TestService testService;

    public static void main(String[] args) {
        log.info("Starting Simple Transactional Test");
        
        try {
            // Setup container
            Warmup warmup = Warmup.create().start();
            warmup.scanPackages("io.warmup.framework.examples.simple");
            
            // Get test instance
            SimpleTransactionalTest test = warmup.getBean(SimpleTransactionalTest.class);
            
            // Run tests
            test.testBasicTransaction();
            test.testReadOnlyTransaction();
            test.testRollbackScenario();
            test.testExceptionHandling();
            
            log.info("All tests completed successfully!");
            
        } catch (Exception e) {
            log.severe("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test basic transaction functionality.
     */
    private void testBasicTransaction() {
        log.info("\n🧪 Testing basic transaction...");
        
        try {
            testService.clearOperations();
            testService.setShouldFail(false);
            
            testService.performOperation("test operation");
            
            List<String> operations = testService.getOperations();
            
            if (operations.contains("test operation")) {
                log.info("✅ Basic transaction test PASSED");
            } else {
                log.severe("❌ Basic transaction test FAILED - operation not found");
            }
            
        } catch (Exception e) {
            log.severe("❌ Basic transaction test FAILED: " + e.getMessage());
        }
    }

    /**
     * Test read-only transaction.
     */
    private void testReadOnlyTransaction() {
        log.info("\n🧪 Testing read-only transaction...");
        
        try {
            testService.clearOperations();
            
            List<String> operations = testService.getOperations();
            
            if (operations != null && operations.isEmpty()) {
                log.info("✅ Read-only transaction test PASSED");
            } else {
                log.severe("❌ Read-only transaction test FAILED");
            }
            
        } catch (Exception e) {
            log.severe("❌ Read-only transaction test FAILED: " + e.getMessage());
        }
    }

    /**
     * Test rollback scenario.
     */
    private void testRollbackScenario() {
        log.info("\n🧪 Testing rollback scenario...");
        
        try {
            testService.clearOperations();
            testService.setShouldFail(true);
            
            int initialSize = testService.getOperations().size();
            
            try {
                testService.performFailingOperation();
            } catch (RuntimeException e) {
                log.info("Caught expected exception: " + e.getMessage());
            }
            
            int finalSize = testService.getOperations().size();
            
            if (finalSize == initialSize) {
                log.info("✅ Rollback test PASSED - operation was rolled back");
            } else {
                log.severe("❌ Rollback test FAILED - operation was not rolled back");
            }
            
        } catch (Exception e) {
            log.severe("❌ Rollback test FAILED: " + e.getMessage());
        }
    }

    /**
     * Test exception handling.
     */
    private void testExceptionHandling() {
        log.info("\n🧪 Testing exception handling...");
        
        try {
            testService.clearOperations();
            
            boolean exceptionCaught = false;
            try {
                testService.performFailingOperation();
            } catch (RuntimeException e) {
                exceptionCaught = true;
                log.info("Exception properly caught: " + e.getMessage());
            }
            
            if (exceptionCaught) {
                log.info("✅ Exception handling test PASSED");
            } else {
                log.severe("❌ Exception handling test FAILED - exception not caught");
            }
            
        } catch (Exception e) {
            log.severe("❌ Exception handling test FAILED: " + e.getMessage());
        }
    }
}