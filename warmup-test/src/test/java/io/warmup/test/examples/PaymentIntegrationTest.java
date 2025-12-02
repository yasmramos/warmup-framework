package io.warmup.test.examples;

import io.warmup.test.annotation.*;
import io.warmup.test.config.TestMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Ejemplo 3: Test de Integración con Modo Mixto
 * 
 * Demuestra:
 * - Uso de TestMode.INTEGRATION
 * - @RealBean para beans reales
 * - @Spy con realImplementation=false para usar mocks
 * - Testing de integración parcial
 */
@ExtendWith(io.warmup.test.core.WarmupTestExtension.class)
@WarmupTest(
    mode = TestMode.INTEGRATION,
    autoMock = true,
    warmupTime = "500ms",
    verbose = false
)
public class PaymentIntegrationTest {
    
    // Sistema bajo test - Usar implementación real con mocks inyectados
    @Spy(realImplementation = true)
    private PaymentService paymentService;
    
    // Gateway externo - Siempre mockear en tests de integración
    @Mock
    private BankGateway bankGateway;
    
    // Servicio de notificaciones - Usar implementación real
    @RealBean
    private NotificationService notificationService;
    
    // Repository de transacciones - Bean real del contexto
    @RealBean
    private TransactionRepository transactionRepository;
    
    // Cache de usuarios - Usar spy sobre mock para testing controlado
    @Spy(realImplementation = false)
    private UserCache userCache;
    
    @Test
    public void processValidPayment_completeFlow_success() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        BankResponse bankResponse = BankResponse.builder()
            .transactionId("TXN-12345")
            .status("SUCCESS")
            .responseCode("00")
            .build();
        
        // Configurar comportamiento del gateway bancario
        when(bankGateway.processPayment(any(PaymentRequest.class)))
            .thenReturn(bankResponse);
        
        // Mockear comportamiento del cache para testing
        when(userCache.getUser(anyString())).thenReturn(createTestUser());
        
        // Act
        PaymentResult result = paymentService.processPayment(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).isEqualTo("TXN-12345");
        assertThat(result.getProcessedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        
        // Verificar integración con servicios reales
        verify(bankGateway).processPayment(request);
        verify(transactionRepository).save(any(Transaction.class));
        verify(notificationService).sendPaymentConfirmation(
            eq(request.getUserId()), 
            eq("TXN-12345"), 
            eq(result.getProcessedAt())
        );
        
        // Verificar que el cache fue usado
        verify(userCache).getUser(request.getUserId());
    }
    
    @Test
    public void processPayment_bankGatewayFailure_rollbackTransaction() {
        // Arrange
        PaymentRequest request = createValidPaymentRequest();
        BankResponse bankResponse = BankResponse.builder()
            .transactionId(null)
            .status("FAILED")
            .responseCode("05")
            .errorMessage("Insufficient funds")
            .build();
        
        when(bankGateway.processPayment(request)).thenReturn(bankResponse);
        
        // Act
        PaymentResult result = paymentService.processPayment(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Insufficient funds");
        assertThat(result.getTransactionId()).isNull();
        
        // Verificar rollback
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(notificationService).sendPaymentFailure(
            eq(request.getUserId()),
            eq("Insufficient funds"),
            any(LocalDateTime.class)
        );
    }
    
    @Test
    public void processPayment_userNotFound_throwsException() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .userId("USER-NOT-FOUND")
            .amount(100.0)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardInfo(createValidCardInfo())
            .build();
        
        // Simular usuario no encontrado en cache
        when(userCache.getUser("USER-NOT-FOUND")).thenReturn(null);
        
        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User USER-NOT-FOUND not found");
        
        // Verificar que el gateway bancario no fue llamado
        verify(bankGateway, never()).processPayment(any(PaymentRequest.class));
    }
    
    private PaymentRequest createValidPaymentRequest() {
        return PaymentRequest.builder()
            .userId("USER-123")
            .amount(250.50)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardInfo(createValidCardInfo())
            .description("Test payment")
            .build();
    }
    
    private CardInfo createValidCardInfo() {
        return CardInfo.builder()
            .cardNumber("4532123456789012")
            .expiryMonth(12)
            .expiryYear(2026)
            .cvv("123")
            .cardHolderName("John Doe")
            .build();
    }
    
    private User createTestUser() {
        return User.builder()
            .id("USER-123")
            .email("john.doe@example.com")
            .name("John Doe")
            .build();
    }
    
    // Clases del dominio
    
    public static class PaymentService {
        private final BankGateway bankGateway;
        private final TransactionRepository transactionRepository;
        private final NotificationService notificationService;
        private final UserCache userCache;
        
        public PaymentService(BankGateway bankGateway, 
                            TransactionRepository transactionRepository,
                            NotificationService notificationService,
                            UserCache userCache) {
            this.bankGateway = bankGateway;
            this.transactionRepository = transactionRepository;
            this.notificationService = notificationService;
            this.userCache = userCache;
        }
        
        public PaymentResult processPayment(PaymentRequest request) {
            try {
                // Step 1: Validar usuario
                User user = userCache.getUser(request.getUserId());
                if (user == null) {
                    throw new UserNotFoundException("User " + request.getUserId() + " not found");
                }
                
                // Step 2: Procesar con gateway bancario
                BankResponse bankResponse = bankGateway.processPayment(request);
                
                // Step 3: Crear transacción
                Transaction transaction = Transaction.builder()
                    .id(generateTransactionId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .status(mapBankStatusToTransactionStatus(bankResponse.getStatus()))
                    .bankTransactionId(bankResponse.getTransactionId())
                    .processedAt(LocalDateTime.now())
                    .build();
                
                // Step 4: Guardar transacción
                if ("SUCCESS".equals(bankResponse.getStatus())) {
                    transactionRepository.save(transaction);
                    notificationService.sendPaymentConfirmation(
                        request.getUserId(), 
                        transaction.getId(), 
                        transaction.getProcessedAt()
                    );
                } else {
                    notificationService.sendPaymentFailure(
                        request.getUserId(),
                        bankResponse.getErrorMessage(),
                        LocalDateTime.now()
                    );
                }
                
                // Step 5: Retornar resultado
                return PaymentResult.builder()
                    .success("SUCCESS".equals(bankResponse.getStatus()))
                    .transactionId(transaction.getId())
                    .processedAt(transaction.getProcessedAt())
                    .errorMessage(bankResponse.getErrorMessage())
                    .build();
                
            } catch (Exception e) {
                notificationService.sendPaymentFailure(
                    request.getUserId(),
                    "System error: " + e.getMessage(),
                    LocalDateTime.now()
                );
                
                return PaymentResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
            }
        }
        
        private String generateTransactionId() {
            return "TXN-" + System.currentTimeMillis();
        }
        
        private TransactionStatus mapBankStatusToTransactionStatus(String bankStatus) {
            return "SUCCESS".equals(bankStatus) ? 
                TransactionStatus.COMPLETED : TransactionStatus.FAILED;
        }
    }
    
    // DTOs y entidades
    
    public static class PaymentRequest {
        private String userId;
        private double amount;
        private PaymentMethod paymentMethod;
        private CardInfo cardInfo;
        private String description;
        
        private PaymentRequest(Builder builder) {
            this.userId = builder.userId;
            this.amount = builder.amount;
            this.paymentMethod = builder.paymentMethod;
            this.cardInfo = builder.cardInfo;
            this.description = builder.description;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String userId;
            private double amount;
            private PaymentMethod paymentMethod;
            private CardInfo cardInfo;
            private String description;
            
            public Builder userId(String userId) { this.userId = userId; return this; }
            public Builder amount(double amount) { this.amount = amount; return this; }
            public Builder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
            public Builder cardInfo(CardInfo cardInfo) { this.cardInfo = cardInfo; return this; }
            public Builder description(String description) { this.description = description; return this; }
            
            public PaymentRequest build() { return new PaymentRequest(this); }
        }
    }
    
    public static class BankResponse {
        private String transactionId;
        private String status;
        private String responseCode;
        private String errorMessage;
        
        private BankResponse(Builder builder) {
            this.transactionId = builder.transactionId;
            this.status = builder.status;
            this.responseCode = builder.responseCode;
            this.errorMessage = builder.errorMessage;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String transactionId;
            private String status;
            private String responseCode;
            private String errorMessage;
            
            public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder responseCode(String responseCode) { this.responseCode = responseCode; return this; }
            public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            
            public BankResponse build() { return new BankResponse(this); }
        }
    }
    
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private LocalDateTime processedAt;
        private String errorMessage;
        
        private PaymentResult(Builder builder) {
            this.success = builder.success;
            this.transactionId = builder.transactionId;
            this.processedAt = builder.processedAt;
            this.errorMessage = builder.errorMessage;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public LocalDateTime getProcessedAt() { return processedAt; }
        public String getErrorMessage() { return errorMessage; }
        
        public static class Builder {
            private boolean success;
            private String transactionId;
            private LocalDateTime processedAt;
            private String errorMessage;
            
            public Builder success(boolean success) { this.success = success; return this; }
            public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
            public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
            public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            
            public PaymentResult build() { return new PaymentResult(this); }
        }
    }
    
    public static class Transaction {
        private String id;
        private String userId;
        private double amount;
        private TransactionStatus status;
        private String bankTransactionId;
        private LocalDateTime processedAt;
        
        private Transaction(Builder builder) {
            this.id = builder.id;
            this.userId = builder.userId;
            this.amount = builder.amount;
            this.status = builder.status;
            this.bankTransactionId = builder.bankTransactionId;
            this.processedAt = builder.processedAt;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String id;
            private String userId;
            private double amount;
            private TransactionStatus status;
            private String bankTransactionId;
            private LocalDateTime processedAt;
            
            public Builder id(String id) { this.id = id; return this; }
            public Builder userId(String userId) { this.userId = userId; return this; }
            public Builder amount(double amount) { this.amount = amount; return this; }
            public Builder status(TransactionStatus status) { this.status = status; return this; }
            public Builder bankTransactionId(String bankTransactionId) { this.bankTransactionId = bankTransactionId; return this; }
            public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
            
            public Transaction build() { return new Transaction(this); }
        }
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
    
    // Interfaces de servicios
    
    public interface BankGateway {
        BankResponse processPayment(PaymentRequest request);
    }
    
    public interface NotificationService {
        void sendPaymentConfirmation(String userId, String transactionId, LocalDateTime processedAt);
        void sendPaymentFailure(String userId, String errorMessage, LocalDateTime failedAt);
    }
    
    public interface TransactionRepository {
        void save(Transaction transaction);
        Transaction findById(String id);
    }
    
    public interface UserCache {
        User getUser(String userId);
        void cacheUser(User user);
    }
    
    // Entidades
    
    public static class User {
        private String id;
        private String email;
        private String name;
        
        private User(Builder builder) {
            this.id = builder.id;
            this.email = builder.email;
            this.name = builder.name;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        
        public static class Builder {
            private String id;
            private String email;
            private String name;
            
            public Builder id(String id) { this.id = id; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder name(String name) { this.name = name; return this; }
            
            public User build() { return new User(this); }
        }
    }
    
    public static class CardInfo {
        private String cardNumber;
        private int expiryMonth;
        private int expiryYear;
        private String cvv;
        private String cardHolderName;
        
        private CardInfo(Builder builder) {
            this.cardNumber = builder.cardNumber;
            this.expiryMonth = builder.expiryMonth;
            this.expiryYear = builder.expiryYear;
            this.cvv = builder.cvv;
            this.cardHolderName = builder.cardHolderName;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public String getCardNumber() { return cardNumber; }
        public int getExpiryMonth() { return expiryMonth; }
        public int getExpiryYear() { return expiryYear; }
        public String getCvv() { return cvv; }
        public String getCardHolderName() { return cardHolderName; }
        
        public static class Builder {
            private String cardNumber;
            private int expiryMonth;
            private int expiryYear;
            private String cvv;
            private String cardHolderName;
            
            public Builder cardNumber(String cardNumber) { this.cardNumber = cardNumber; return this; }
            public Builder expiryMonth(int expiryMonth) { this.expiryMonth = expiryMonth; return this; }
            public Builder expiryYear(int expiryYear) { this.expiryYear = expiryYear; return this; }
            public Builder cvv(String cvv) { this.cvv = cvv; return this; }
            public Builder cardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; return this; }
            
            public CardInfo build() { return new CardInfo(this); }
        }
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
    }
    
    // Excepciones
    
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}