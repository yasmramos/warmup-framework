package io.warmup.test.examples;

import io.warmup.test.annotation.*;
import io.warmup.test.config.TestMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Ejemplo 1: Test Unitario Simple
 * 
 * Demuestra el uso básico de @WarmupTest con @Mock y @Spy
 * para testing unitario zero-config.
 */
@ExtendWith(io.warmup.test.core.WarmupTestExtension.class)
@WarmupTest
public class UserServiceTest {
    
    // Sistema bajo test - Spy con implementación real e inyección automática
    @Spy
    private UserService userService;
    
    // Dependencias externas - Mocks completos
    @Mock
    private UserRepository userRepo;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Test
    public void createUser_validUser_createsSuccessfully() {
        // Arrange
        User user = new User("john@example.com", "password123");
        User savedUser = user.toBuilder().id(1L).build();
        
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        
        // Act
        User result = userService.createUser(user);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        
        // Verify interactions
        verify(userRepo).save(any(User.class));
        verify(emailService).sendWelcomeEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
    }
    
    @Test
    public void createUser_duplicateEmail_throwsException() {
        // Arrange
        User user = new User("existing@example.com", "password123");
        User existingUser = user.toBuilder().id(1L).build();
        
        when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(DuplicateUserException.class)
            .hasMessage("User with email existing@example.com already exists");
        
        // Verify no save attempted
        verify(userRepo, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(anyString());
    }
    
    @Test
    public void createUser_invalidEmail_throwsException() {
        // Arrange
        User user = new User("invalid-email", "password123");
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessage("Invalid email format");
    }
    
    // Clases de ejemplo para el test
    public static class UserService {
        private final UserRepository userRepo;
        private final EmailService emailService;
        private final PasswordEncoder passwordEncoder;
        
        public UserService(UserRepository userRepo, EmailService emailService, PasswordEncoder passwordEncoder) {
            this.userRepo = userRepo;
            this.emailService = emailService;
            this.passwordEncoder = passwordEncoder;
        }
        
        public User createUser(User user) {
            // Validaciones
            if (userRepo.findByEmail(user.getEmail()).isPresent()) {
                throw new DuplicateUserException("User with email " + user.getEmail() + " already exists");
            }
            
            if (!isValidEmail(user.getEmail())) {
                throw new InvalidEmailException("Invalid email format");
            }
            
            // Crear usuario
            User newUser = user.toBuilder()
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
            
            User savedUser = userRepo.save(newUser);
            emailService.sendWelcomeEmail(savedUser.getEmail());
            
            return savedUser;
        }
        
        private boolean isValidEmail(String email) {
            return email.contains("@") && email.contains(".");
        }
    }
    
    public static class User {
        private Long id;
        private String email;
        private String password;
        
        public User(String email, String password) {
            this.email = email;
            this.password = password;
        }
        
        private User(Builder builder) {
            this.id = builder.id;
            this.email = builder.email;
            this.password = builder.password;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        
        public static class Builder {
            private Long id;
            private String email;
            private String password;
            
            public Builder id(Long id) { this.id = id; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder password(String password) { this.password = password; return this; }
            
            public User build() {
                return new User(this);
            }
        }
    }
    
    public interface UserRepository {
        User save(User user);
        Optional<User> findByEmail(String email);
    }
    
    public interface EmailService {
        void sendWelcomeEmail(String email);
    }
    
    public interface PasswordEncoder {
        String encode(String rawPassword);
    }
    
    public static class DuplicateUserException extends RuntimeException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }
    
    public static class InvalidEmailException extends RuntimeException {
        public InvalidEmailException(String message) {
            super(message);
        }
    }
}