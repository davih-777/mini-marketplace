package com.marketplace.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserAuthDTO(@NotBlank String email, @NotBlank String password) {}
