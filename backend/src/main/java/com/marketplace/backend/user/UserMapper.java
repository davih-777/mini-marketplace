package com.marketplace.backend.user;

import static com.marketplace.backend.user.auth.jwt.JWTService.ONE_DAY_MILLIS;

import com.marketplace.backend.enums.UserRole;
import com.marketplace.backend.models.User;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toEntity(UserRegistrationDTO userDTO) {
    OffsetDateTime now = OffsetDateTime.now();

    return User.builder()
        .name(userDTO.name())
        .email(userDTO.email())
        .birthDate(userDTO.birthDate())
        .role(UserRole.ROLE_USER)
        .isActive(true)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  public UserResponseDTO toDTO(User user, String token) {
    return new UserResponseDTO(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        new UserResponseDTO.TokenDTO(token, "Bearer", ONE_DAY_MILLIS));
  }
}
