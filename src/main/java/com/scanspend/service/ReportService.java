package com.scanspend.service;

import com.scanspend.dto.MonthlyReportDto;
import com.scanspend.model.User;
import com.scanspend.repository.ExpenseRepository;
import com.scanspend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ReportService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public MonthlyReportDto monthly(String email, YearMonth month) {
        User user = userRepository.findByEmail(email).orElseThrow();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        BigDecimal total = expenseRepository.totalBetween(user, start, end);
        Map<String, BigDecimal> byCategory = new HashMap<>();
        expenseRepository.sumByCategoryBetween(user, start, end).forEach(map -> {
            String cat = (String) map.get("category");
            BigDecimal amt = (BigDecimal) map.get("total");
            byCategory.put(cat, amt);
        });
        return new MonthlyReportDto(month.toString(), total == null ? BigDecimal.ZERO : total, byCategory);
    }
}
