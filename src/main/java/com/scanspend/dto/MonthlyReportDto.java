package com.scanspend.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlyReportDto(String month,
                               BigDecimal total,
                               Map<String, BigDecimal> byCategory) { }
