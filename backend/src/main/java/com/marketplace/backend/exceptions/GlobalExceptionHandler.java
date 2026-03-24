package com.marketplace.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<CustomErrorResponse> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    String message = "Data integrity error.";
    HttpStatus status = HttpStatus.BAD_REQUEST;

    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("uk_user_email")) {
      message = "E-mail already registered.";
      status = HttpStatus.CONFLICT;
      log.warn(
          "Database conflict: Attempt to register a duplicate email at {}",
          request.getRequestURI());
    } else {
      log.error("Database integrity violation: {}", ex.getMessage());
    }

    return buildResponse(status, message, request);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<CustomErrorResponse> handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "E-mail or password does not match.", request);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<CustomErrorResponse> handleUserNotFound(
      UsernameNotFoundException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "E-mail or password does not match.", request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<CustomErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred: ", ex);

    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred on the server.", request);
  }

  private ResponseEntity<CustomErrorResponse> buildResponse(
      HttpStatus status, String message, HttpServletRequest request) {
    CustomErrorResponse error =
        new CustomErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI());
    return ResponseEntity.status(status).body(error);
  }
}
