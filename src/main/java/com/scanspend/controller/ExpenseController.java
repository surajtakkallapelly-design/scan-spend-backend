package com.scanspend.controller;

import com.scanspend.dto.ExpenseDto;
import com.scanspend.model.Expense;
import com.scanspend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> list(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.list(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<Expense> create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody ExpenseDto dto) {
        return ResponseEntity.ok(expenseService.create(user.getUsername(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody ExpenseDto dto) {
        return ResponseEntity.ok(expenseService.update(user.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        expenseService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
