package com.scanspend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDto(Long id,
                         @NotBlank String merchant,
                         @NotNull BigDecimal amount,
                         @NotBlank String category,
                         @NotNull LocalDate date,
                         String receiptImage) {
}
