package com.marketplace.backend.user;

import com.marketplace.backend.exceptions.InvalidTokenException;
import com.marketplace.backend.models.User;
import com.marketplace.backend.user.auth.UserAuthService;
import com.marketplace.backend.user.auth.UserDetailsImpl;
import com.marketplace.backend.user.auth.jwt.JWTService;
import com.marketplace.backend.user.dto.UserAuthDTO;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    String accessToken =
        jwtService.generateAccessToken(persistedUser.getEmail(), persistedUser.getRole());

    return mapper.toDTO(persistedUser, accessToken);
  }

  public UserResponseDTO verify(UserAuthDTO dto) {
    Authentication authentication =
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userDetails.getUser();

    String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());

    return mapper.toDTO(user, accessToken);
  }

  public ResponseCookie generateAuthCookie(String email) {
    String refreshToken = jwtService.generateRefreshToken(email);
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // must be true in production environment
        .path("/")
        .maxAge(604800) // 7 days
        .sameSite("Strict")
        .build();
  }

  public UserResponseDTO refresh(String refreshToken) {
    if (jwtService.validateRefreshToken(refreshToken)) {
      String email = jwtService.extractEmailFromToken(refreshToken);
      User user =
          repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
      String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());

      return mapper.toDTO(user, accessToken);
    } else {
      throw new InvalidTokenException("Token is not valid.");
    }
  }
}
