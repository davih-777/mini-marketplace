package com.marketplace.backend.user;

import com.marketplace.backend.user.dto.UserAuthDTO;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  private ResponseEntity<?> registerNewUser(@RequestBody @Valid UserRegistrationDTO userDTO) {
    log.info("Request received to register user: {}", userDTO.email());

    UserResponseDTO user = service.register(userDTO);

    log.info("User {} successfully registered with ID {}.", user.email(), user.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/login")
  private ResponseEntity<UserResponseDTO> login(@RequestBody UserAuthDTO dto) {
    log.debug("Login attempt for user: {}", dto.email());
    UserResponseDTO user = service.verify(dto);

    log.debug("User {} authenticated successfully.", dto.email());
    return ResponseEntity.ok().body(user);
  }
}
