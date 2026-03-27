package com.marketplace.backend.user.dto;

import com.marketplace.backend.enums.UserRole;

public record UserResponseDTO(
    Long id, String name, String email, UserRole role, String accessToken) {}
