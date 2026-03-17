package com.scanspend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReminderDto(Long id, String title, BigDecimal amount, LocalDate dueDate, boolean notified) { }
