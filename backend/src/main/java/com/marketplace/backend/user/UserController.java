package com.marketplace.backend.user;

import com.marketplace.backend.user.dto.UserAuthDTO;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @PostMapping("/register")
  private ResponseEntity<?> registerNewUser(
      @RequestBody @Valid UserRegistrationDTO registrationDTO) {
    log.info("Request received to register user: {}", registrationDTO.email());

    UserResponseDTO userDTO = service.register(registrationDTO);
    ResponseCookie authCookie = service.generateAuthCookie(userDTO.email());

    log.info("User {} successfully registered with ID {}.", userDTO.email(), userDTO.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.SET_COOKIE, authCookie.toString())
        .body(userDTO);
  }

  @PostMapping("/login")
  private ResponseEntity<UserResponseDTO> login(@RequestBody UserAuthDTO authDto) {
    log.debug("Login attempt for user: {}", authDto.email());
    UserResponseDTO userDTO = service.verify(authDto);

    ResponseCookie authCookie = service.generateAuthCookie(userDTO.email());

    log.debug("User {} authenticated successfully.", userDTO.email());
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookie.toString()).body(userDTO);
  }

  @PostMapping("/refresh")
  public ResponseEntity<UserResponseDTO> refresh(@CookieValue("refreshToken") String refreshToken) {
    UserResponseDTO userDTO = service.refresh(refreshToken);

    log.debug("Access token refreshed for user: {}", userDTO.email());
    return ResponseEntity.ok(userDTO);
  }
}
