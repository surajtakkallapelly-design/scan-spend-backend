package com.scanspend.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UserSummary(Long id, String name, String email, BigDecimal income, Instant createdAt) { }
