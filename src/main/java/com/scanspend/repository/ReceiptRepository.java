package com.scanspend.repository;

import com.scanspend.model.Receipt;
import com.scanspend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByExpense(Expense expense);
    void deleteByExpense(Expense expense);
}
