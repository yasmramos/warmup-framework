package io.warmup.test.examples;

import io.warmup.test.annotation.*;
import io.warmup.test.config.TestMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Ejemplo 2: Test con Dependencias Complejas y Múltiples Spies
 * 
 * Demuestra:
 * - Múltiples @Spy con auto-inyección de dependencias
 * - Manejo de dependencias complejas
 * - Testing de flujos de negocio complejos
 */
@ExtendWith(io.warmup.test.core.WarmupTestExtension.class)
@WarmupTest(mode = TestMode.UNIT)
public class OrderProcessingTest {
    
    // Múltiples servicios en el flujo - Todos como @Spy para mantener comportamiento real
    @Spy
    private OrderValidator orderValidator;
    
    @Spy
    private PaymentProcessor paymentProcessor;
    
    @Spy
    private InventoryManager inventoryManager;
    
    @Spy
    private ShippingService shippingService;
    
    // Servicio principal - También @Spy con auto-inyección de todas las dependencias
    @Spy
    private OrderService orderService;
    
    // Dependencias externas - Como @Mock
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private EmailService emailService;
    
    @Test
    public void processOrder_completeFlow_success() {
        // Arrange
        Order order = createValidOrder();
        PaymentInfo paymentInfo = createValidPaymentInfo();
        
        // Configurar comportamientos básicos para validación
        when(orderValidator.validate(order)).thenReturn(ValidationResult.valid());
        when(paymentProcessor.processPayment(paymentInfo)).thenReturn(PaymentResult.success());
        when(inventoryManager.reserveItems(order.getItems())).thenReturn(ReservationResult.success());
        
        // Act
        ProcessingResult result = orderService.processOrder(order, paymentInfo);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isNotNull();
        
        // Verificar que todas las operaciones fueron llamadas
        verify(orderValidator).validate(order);
        verify(paymentProcessor).processPayment(paymentInfo);
        verify(inventoryManager).reserveItems(order.getItems());
        verify(shippingService).scheduleShipping(eq(result.getOrderId()), any(ShippingInfo.class));
        verify(notificationService).sendOrderConfirmation(order.getCustomerId(), result.getOrderId());
        verify(auditService).logOrderProcessed(eq(order.getId()), eq("SUCCESS"));
    }
    
    @Test
    public void processOrder_validationFailed_returnsError() {
        // Arrange
        Order order = createInvalidOrder();
        PaymentInfo paymentInfo = createValidPaymentInfo();
        
        ValidationResult validationResult = ValidationResult.error("Invalid shipping address");
        when(orderValidator.validate(order)).thenReturn(validationResult);
        
        // Act
        ProcessingResult result = orderService.processOrder(order, paymentInfo);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Invalid shipping address");
        
        // Verificar que las operaciones posteriores no fueron llamadas
        verify(paymentProcessor, never()).processPayment(any(PaymentInfo.class));
        verify(inventoryManager, never()).reserveItems(anyList());
        verify(shippingService, never()).scheduleShipping(anyLong(), any(ShippingInfo.class));
        verify(auditService).logOrderProcessed(eq(order.getId()), eq("VALIDATION_FAILED"));
    }
    
    @Test
    public void processOrder_paymentFailed_rollbackInventory() {
        // Arrange
        Order order = createValidOrder();
        PaymentInfo paymentInfo = createFailedPaymentInfo();
        
        when(orderValidator.validate(order)).thenReturn(ValidationResult.valid());
        PaymentResult paymentResult = PaymentResult.error("Credit card declined");
        when(paymentProcessor.processPayment(paymentInfo)).thenReturn(paymentResult);
        
        // Act
        ProcessingResult result = orderService.processOrder(order, paymentInfo);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Credit card declined");
        
        // Verificar rollback de inventario
        verify(inventoryManager).releaseItems(order.getItems());
        verify(auditService).logOrderProcessed(eq(order.getId()), eq("PAYMENT_FAILED"));
    }
    
    private Order createValidOrder() {
        return Order.builder()
            .id(123L)
            .customerId(456L)
            .items(List.of(
                new OrderItem("LAPTOP-001", 1, 999.99),
                new OrderItem("MOUSE-001", 2, 29.99)
            ))
            .shippingAddress("123 Main St, City, State 12345")
            .build();
    }
    
    private Order createInvalidOrder() {
        return Order.builder()
            .id(124L)
            .customerId(456L)
            .items(List.of(new OrderItem("INVALID", 1, 0.01)))
            .shippingAddress("") // Invalid: empty address
            .build();
    }
    
    private PaymentInfo createValidPaymentInfo() {
        return PaymentInfo.builder()
            .amount(1059.97) // Total de los items
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardNumber("4532123456789012")
            .expiryDate("12/26")
            .cvv("123")
            .build();
    }
    
    private PaymentInfo createFailedPaymentInfo() {
        return PaymentInfo.builder()
            .amount(1059.97)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardNumber("4000000000000002") // Declined card number
            .expiryDate("12/26")
            .cvv("123")
            .build();
    }
    
    // Clases del dominio
    
    public static class OrderService {
        private final OrderValidator orderValidator;
        private final PaymentProcessor paymentProcessor;
        private final InventoryManager inventoryManager;
        private final ShippingService shippingService;
        private final NotificationService notificationService;
        private final AuditService auditService;
        
        public OrderService(OrderValidator orderValidator, PaymentProcessor paymentProcessor,
                          InventoryManager inventoryManager, ShippingService shippingService,
                          NotificationService notificationService, AuditService auditService) {
            this.orderValidator = orderValidator;
            this.paymentProcessor = paymentProcessor;
            this.inventoryManager = inventoryManager;
            this.shippingService = shippingService;
            this.notificationService = notificationService;
            this.auditService = auditService;
        }
        
        public ProcessingResult processOrder(Order order, PaymentInfo paymentInfo) {
            try {
                // Step 1: Validate order
                ValidationResult validation = orderValidator.validate(order);
                if (!validation.isValid()) {
                    auditService.logOrderProcessed(order.getId(), "VALIDATION_FAILED");
                    return ProcessingResult.error(validation.getErrorMessage());
                }
                
                // Step 2: Reserve inventory
                ReservationResult inventoryResult = inventoryManager.reserveItems(order.getItems());
                if (!inventoryResult.isSuccess()) {
                    auditService.logOrderProcessed(order.getId(), "INVENTORY_UNAVAILABLE");
                    return ProcessingResult.error("Items not available");
                }
                
                // Step 3: Process payment
                PaymentResult paymentResult = paymentProcessor.processPayment(paymentInfo);
                if (!paymentResult.isSuccess()) {
                    // Rollback inventory
                    inventoryManager.releaseItems(order.getItems());
                    auditService.logOrderProcessed(order.getId(), "PAYMENT_FAILED");
                    return ProcessingResult.error(paymentResult.getErrorMessage());
                }
                
                // Step 4: Schedule shipping
                Long orderId = generateOrderId();
                ShippingInfo shippingInfo = createShippingInfo(order, paymentInfo);
                shippingService.scheduleShipping(orderId, shippingInfo);
                
                // Step 5: Send confirmation
                notificationService.sendOrderConfirmation(order.getCustomerId(), orderId);
                auditService.logOrderProcessed(order.getId(), "SUCCESS");
                
                return ProcessingResult.success(orderId);
                
            } catch (Exception e) {
                auditService.logOrderProcessed(order.getId(), "SYSTEM_ERROR");
                return ProcessingResult.error("System error: " + e.getMessage());
            }
        }
        
        private Long generateOrderId() {
            return System.currentTimeMillis() % 1000000L;
        }
        
        private ShippingInfo createShippingInfo(Order order, PaymentInfo paymentInfo) {
            return ShippingInfo.builder()
                .orderId(generateOrderId())
                .shippingAddress(order.getShippingAddress())
                .estimatedDelivery(System.currentTimeMillis() + (24 * 60 * 60 * 1000))
                .build();
        }
    }
    
    public static class Order {
        private Long id;
        private Long customerId;
        private List<OrderItem> items;
        private String shippingAddress;
        
        private Order(Builder builder) {
            this.id = builder.id;
            this.customerId = builder.customerId;
            this.items = builder.items;
            this.shippingAddress = builder.shippingAddress;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public Long getId() { return id; }
        public Long getCustomerId() { return customerId; }
        public List<OrderItem> getItems() { return items; }
        public String getShippingAddress() { return shippingAddress; }
        
        public static class Builder {
            private Long id;
            private Long customerId;
            private List<OrderItem> items;
            private String shippingAddress;
            
            public Builder id(Long id) { this.id = id; return this; }
            public Builder customerId(Long customerId) { this.customerId = customerId; return this; }
            public Builder items(List<OrderItem> items) { this.items = items; return this; }
            public Builder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
            
            public Order build() { return new Order(this); }
        }
    }
    
    public static class OrderItem {
        private String productId;
        private int quantity;
        private double unitPrice;
        
        public OrderItem(String productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
    }
    
    public static class PaymentInfo {
        private double amount;
        private PaymentMethod paymentMethod;
        private String cardNumber;
        private String expiryDate;
        private String cvv;
        
        private PaymentInfo(Builder builder) {
            this.amount = builder.amount;
            this.paymentMethod = builder.paymentMethod;
            this.cardNumber = builder.cardNumber;
            this.expiryDate = builder.expiryDate;
            this.cvv = builder.cvv;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private double amount;
            private PaymentMethod paymentMethod;
            private String cardNumber;
            private String expiryDate;
            private String cvv;
            
            public Builder amount(double amount) { this.amount = amount; return this; }
            public Builder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
            public Builder cardNumber(String cardNumber) { this.cardNumber = cardNumber; return this; }
            public Builder expiryDate(String expiryDate) { this.expiryDate = expiryDate; return this; }
            public Builder cvv(String cvv) { this.cvv = cvv; return this; }
            
            public PaymentInfo build() { return new PaymentInfo(this); }
        }
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
    }
    
    // Interfaces de los servicios
    
    public interface OrderValidator {
        ValidationResult validate(Order order);
    }
    
    public interface PaymentProcessor {
        PaymentResult processPayment(PaymentInfo paymentInfo);
    }
    
    public interface InventoryManager {
        ReservationResult reserveItems(List<OrderItem> items);
        void releaseItems(List<OrderItem> items);
    }
    
    public interface ShippingService {
        void scheduleShipping(Long orderId, ShippingInfo shippingInfo);
    }
    
    public interface NotificationService {
        void sendOrderConfirmation(Long customerId, Long orderId);
    }
    
    public interface AuditService {
        void logOrderProcessed(Long orderId, String status);
    }
    
    // Resultados y información
    
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class PaymentResult {
        private boolean success;
        private String errorMessage;
        
        private PaymentResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static PaymentResult success() {
            return new PaymentResult(true, null);
        }
        
        public static PaymentResult error(String message) {
            return new PaymentResult(false, message);
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class ReservationResult {
        private boolean success;
        private String errorMessage;
        
        private ReservationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static ReservationResult success() {
            return new ReservationResult(true, null);
        }
        
        public static ReservationResult error(String message) {
            return new ReservationResult(false, message);
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class ProcessingResult {
        private boolean success;
        private Long orderId;
        private String errorMessage;
        
        private ProcessingResult(boolean success, Long orderId, String errorMessage) {
            this.success = success;
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }
        
        public static ProcessingResult success(Long orderId) {
            return new ProcessingResult(true, orderId, null);
        }
        
        public static ProcessingResult error(String message) {
            return new ProcessingResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public Long getOrderId() { return orderId; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class ShippingInfo {
        private Long orderId;
        private String shippingAddress;
        private long estimatedDelivery;
        
        private ShippingInfo(Builder builder) {
            this.orderId = builder.orderId;
            this.shippingAddress = builder.shippingAddress;
            this.estimatedDelivery = builder.estimatedDelivery;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private Long orderId;
            private String shippingAddress;
            private long estimatedDelivery;
            
            public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
            public Builder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
            public Builder estimatedDelivery(long estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; return this; }
            
            public ShippingInfo build() { return new ShippingInfo(this); }
        }
    }
}