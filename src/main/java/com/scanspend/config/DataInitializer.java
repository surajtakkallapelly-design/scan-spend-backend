package com.scanspend.config;

import com.scanspend.model.User;
import com.scanspend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            createIfAbsent(users, encoder, "Suraj", "suraj@example.com", "demo123");
            createIfAbsent(users, encoder, "Alice", "alice@example.com", "123456");
        };
    }

    private void createIfAbsent(UserRepository users, PasswordEncoder encoder, String name, String email, String rawPassword) {
        users.findByEmail(email).ifPresentOrElse(
                u -> { /* already there */ },
                () -> {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setIncome(new java.math.BigDecimal("100000"));
                    user.setPassword(encoder.encode(rawPassword));
                    users.save(user);
                }
        );
    }
}
