package com.scanspend.dto;

import java.math.BigDecimal;

public record AuthResponse(String token, String email, String name, BigDecimal income) { }
