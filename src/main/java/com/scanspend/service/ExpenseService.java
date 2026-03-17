package com.scanspend.service;

import com.scanspend.dto.ExpenseDto;
import com.scanspend.exception.ApiException;
import com.scanspend.model.Expense;
import com.scanspend.model.User;
import com.scanspend.repository.ExpenseRepository;
import com.scanspend.repository.UserRepository;
import com.scanspend.repository.ReceiptRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository, ReceiptRepository receiptRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.receiptRepository = receiptRepository;
    }

    public List<Expense> list(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return expenseRepository.findByUser(user);
    }

    public Expense create(String email, ExpenseDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Expense expense = new Expense();
        expense.setMerchant(dto.merchant());
        expense.setAmount(dto.amount());
        expense.setCategory(dto.category());
        expense.setDate(dto.date());
        expense.setReceiptImage(dto.receiptImage());
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public Expense update(String email, Long id, ExpenseDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException("Expense not found", HttpStatus.NOT_FOUND));
        expense.setMerchant(dto.merchant());
        expense.setAmount(dto.amount());
        expense.setCategory(dto.category());
        expense.setDate(dto.date());
        expense.setReceiptImage(dto.receiptImage());
        return expenseRepository.save(expense);
    }

    public void delete(String email, Long id) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException("Expense not found", HttpStatus.NOT_FOUND));
        receiptRepository.findByExpense(expense).ifPresent(receiptRepository::delete);
        expenseRepository.delete(expense);
    }
}
