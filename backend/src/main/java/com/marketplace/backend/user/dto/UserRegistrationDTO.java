package com.marketplace.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UserRegistrationDTO(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    LocalDate birthDate) {}
