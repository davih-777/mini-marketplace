package com.marketplace.backend.user.auth;

import com.marketplace.backend.models.User;
import com.marketplace.backend.models.UserAuth;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

  private final UserAuthMapper mapper;
  private final UserAuthRepository repository;

  public UserAuthService(UserAuthMapper mapper, UserAuthRepository repository) {
    this.mapper = mapper;
    this.repository = repository;
  }

  public void saveUserAuth(User user, String password) {
    UserAuth userAuth = mapper.toEntity(user, password);
    repository.save(userAuth);
  }
}
