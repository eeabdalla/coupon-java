package digits.schwarz.star.couponservice.controller.handler;

import digits.schwarz.star.couponservice.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
    var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> ErrorResponse.FieldError.builder()
            .field(fe.getField())
            .message(fe.getDefaultMessage())
            .rejectedValue(fe.getRejectedValue())
            .build())
        .toList();

    var response = ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Validation Failed")
        .message("One or more fields have invalid values")
        .path(request.getRequestURI())
        .fieldErrors(fieldErrors)
        .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
    var response = ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Malformed Request")
        .message("Request body is missing or contains invalid JSON")
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error on {}", request.getRequestURI(), ex);

    var response = ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("An unexpected error occurred")
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.internalServerError().body(response);
  }
}