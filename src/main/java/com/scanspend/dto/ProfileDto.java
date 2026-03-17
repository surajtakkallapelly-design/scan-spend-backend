package com.scanspend.dto;

import java.math.BigDecimal;

public record ProfileDto(String name, String email, BigDecimal income) { }
