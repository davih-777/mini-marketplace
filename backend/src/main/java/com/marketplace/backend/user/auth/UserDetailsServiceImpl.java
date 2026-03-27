package com.marketplace.backend.user.auth;

import com.marketplace.backend.models.User;
import com.marketplace.backend.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository repository;

  public UserDetailsServiceImpl(UserRepository repository, UserAuthRepository userAuthRepository) {
    this.repository = repository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        repository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found for email:" + email));

    return new UserDetailsImpl(user);
  }
}
