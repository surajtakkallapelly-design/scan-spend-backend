package com.scanspend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank String name,
                          @Email String email,
                          @NotBlank String password) { }
