package com.scanspend.controller;

import com.scanspend.dto.UserSummary;
import com.scanspend.model.User;
import com.scanspend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository users;
    private final String adminKey;

    public AdminController(UserRepository users, @Value("${app.admin-key:}") String adminKey) {
        this.users = users;
        this.adminKey = adminKey;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> listUsers(@RequestParam String key) {
        if (adminKey == null || adminKey.isBlank() || !adminKey.equals(key)) {
            return ResponseEntity.status(403).build();
        }
        List<UserSummary> summaries = users.findAll().stream()
                .map(this::toSummary)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    private UserSummary toSummary(User u) {
        return new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getIncome(), u.getCreatedAt());
    }
}

