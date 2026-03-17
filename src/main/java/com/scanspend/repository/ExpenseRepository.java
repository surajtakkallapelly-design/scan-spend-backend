package com.scanspend.repository;

import com.scanspend.model.Expense;
import com.scanspend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    @Query("select e.category as category, sum(e.amount) as total from Expense e where e.user = :user and e.date between :start and :end group by e.category")
    List<Map<String, Object>> sumByCategoryBetween(User user, LocalDate start, LocalDate end);

    @Query("select sum(e.amount) from Expense e where e.user = :user and e.date between :start and :end")
    java.math.BigDecimal totalBetween(User user, LocalDate start, LocalDate end);
}
