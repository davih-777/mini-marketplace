package com.marketplace.backend.user;

import com.marketplace.backend.models.User;
import com.marketplace.backend.user.auth.UserAuthService;
import com.marketplace.backend.user.auth.UserDetailsImpl;
import com.marketplace.backend.user.auth.jwt.JWTService;
import com.marketplace.backend.user.dto.UserAuthDTO;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final AuthenticationManager authManager;
  private final JWTService jwtService;
  private final PasswordEncoder encoder;
  private final UserAuthService userAuthService;
  private final UserRepository repository;
  private final UserMapper mapper;

  public UserService(
      AuthenticationManager authManager,
      JWTService jwtService,
      PasswordEncoder encoder,
      UserAuthService userAuthService,
      UserRepository repository,
      UserMapper mapper) {
    this.authManager = authManager;
    this.jwtService = jwtService;
    this.encoder = encoder;
    this.userAuthService = userAuthService;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public UserResponseDTO register(UserRegistrationDTO userDTO) {
    String encryptedPassword = encoder.encode(userDTO.password());
    User user = mapper.toEntity(userDTO);

    User persistedUser = repository.save(user);
    userAuthService.saveUserAuth(user, encryptedPassword);
    String token = generateToken(user);

    return mapper.toDTO(persistedUser, token);
  }

  public UserResponseDTO verify(UserAuthDTO dto) {
    Authentication authentication =
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userDetails.getUser();
    String token = generateToken(user);

    return mapper.toDTO(user, token);
  }

  public String generateToken(User user) {
    return jwtService.generateToken(user);
  }
}
