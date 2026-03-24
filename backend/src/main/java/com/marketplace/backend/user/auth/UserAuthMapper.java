package com.marketplace.backend.user.auth;

import com.marketplace.backend.models.User;
import com.marketplace.backend.models.UserAuth;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class UserAuthMapper {

  public UserAuth toEntity(User user, String password) {
    OffsetDateTime now = OffsetDateTime.now();
    return UserAuth.builder().user(user).password(password).createdAt(now).updatedAt(now).build();
  }
}
