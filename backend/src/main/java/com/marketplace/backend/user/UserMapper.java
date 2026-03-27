package com.marketplace.backend.user;

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

  public UserResponseDTO toDTO(User user, String accessToken) {
    return new UserResponseDTO(
        user.getId(), user.getName(), user.getEmail(), user.getRole(), accessToken);
  }
}
