package com.marketplace.backend.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.marketplace.backend.enums.UserRole;
import com.marketplace.backend.exceptions.CustomErrorResponse;
import com.marketplace.backend.user.dto.UserAuthDTO;
import com.marketplace.backend.user.dto.UserRegistrationDTO;
import com.marketplace.backend.user.dto.UserResponseDTO;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserService userService;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  void clearDB() {
    userRepository.deleteAll();
  }

  @Test
  void registerSuccess() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    var response =
        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponseDTO responseDTO =
        objectMapper.readValue(response.getResponse().getContentAsString(), UserResponseDTO.class);

    assertEquals(registrationDTO.email(), responseDTO.email());
    assertEquals(UserRole.ROLE_USER, responseDTO.role());
    assertNotNull(responseDTO.accessToken());
    assertNotNull(response.getResponse().getCookie("refreshToken").getValue());
  }

  @Test
  void registerFailEmailInUse() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    var response =
        mockMvc
            .perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
            .andExpect(status().isConflict())
            .andReturn();

    CustomErrorResponse error =
        objectMapper.readValue(
            response.getResponse().getContentAsString(), CustomErrorResponse.class);

    assertEquals("E-mail already registered.", error.getMessage());
  }

  @Test
  void loginSuccess() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    UserAuthDTO authDTO = new UserAuthDTO(registrationDTO.email(), registrationDTO.password());
    var response =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authDTO)))
            .andExpect(status().isOk())
            .andReturn();

    UserResponseDTO responseDTO =
        objectMapper.readValue(response.getResponse().getContentAsString(), UserResponseDTO.class);

    assertEquals(registrationDTO.email(), responseDTO.email());
    assertEquals(UserRole.ROLE_USER, responseDTO.role());
    assertNotNull(responseDTO.accessToken());
    assertNotNull(response.getResponse().getCookie("refreshToken").getValue());
  }

  @Test
  void loginFailPassword() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    UserAuthDTO authDTO = new UserAuthDTO(registrationDTO.email(), "wrongpass123");
    var response =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authDTO)))
            .andExpect(status().isUnauthorized())
            .andReturn();

    CustomErrorResponse error =
        objectMapper.readValue(
            response.getResponse().getContentAsString(), CustomErrorResponse.class);

    assertEquals("E-mail or password does not match.", error.getMessage());
  }

  @Test
  void loginFailEmail() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    UserAuthDTO authDTO = new UserAuthDTO("wrong-email@test.com", registrationDTO.password());
    var response =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authDTO)))
            .andExpect(status().isUnauthorized())
            .andReturn();

    CustomErrorResponse error =
        objectMapper.readValue(
            response.getResponse().getContentAsString(), CustomErrorResponse.class);

    assertEquals("E-mail or password does not match.", error.getMessage());
  }

  @Test
  void refreshTokenSuccess() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    UserAuthDTO authDTO = new UserAuthDTO(registrationDTO.email(), registrationDTO.password());
    var loginResponse =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authDTO)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie refreshCookie = loginResponse.getResponse().getCookie("refreshToken");

    var refreshResponse =
        mockMvc
            .perform(
                post("/auth/refresh").cookie(refreshCookie).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    UserResponseDTO responseDTO =
        objectMapper.readValue(
            refreshResponse.getResponse().getContentAsString(), UserResponseDTO.class);

    assertEquals(registrationDTO.email(), responseDTO.email());
    assertEquals(UserRole.ROLE_USER, responseDTO.role());
    assertNotNull(responseDTO.accessToken());
  }

  @Test
  void refreshTokenFail() throws Exception {
    UserRegistrationDTO registrationDTO =
        new UserRegistrationDTO(
            "Mock Name", "mock-email@test.com", "mockpass123", LocalDate.of(1999, 11, 11));

    userService.register(registrationDTO);

    UserAuthDTO authDTO = new UserAuthDTO(registrationDTO.email(), registrationDTO.password());
    var loginResponse =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authDTO)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie refreshCookie = loginResponse.getResponse().getCookie("refreshToken");

    refreshCookie.setValue(refreshCookie.getValue() + "invalid");

    var refreshResponse =
        mockMvc
            .perform(
                post("/auth/refresh").cookie(refreshCookie).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andReturn();

    CustomErrorResponse error =
        objectMapper.readValue(
            refreshResponse.getResponse().getContentAsString(), CustomErrorResponse.class);

    assertEquals("Token is not valid.", error.getMessage());
    assertEquals("", refreshResponse.getResponse().getCookie("refreshToken").getValue());
  }
}
