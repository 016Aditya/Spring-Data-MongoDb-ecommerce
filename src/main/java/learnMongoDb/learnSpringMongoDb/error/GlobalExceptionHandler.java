package learnMongoDb.learnSpringMongoDb.error;

import jakarta.servlet.http.HttpServletRequest;
import learnMongoDb.learnSpringMongoDb.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Phase 1, 2 & 3 Security Exceptions ───────────────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountLocked(AccountLockedException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("ACCOUNT_LOCKED")
                .message(ex.getMessage())
                .remainingSeconds(null)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.LOCKED).body(response); // HTTP 423
    }

    @ExceptionHandler(LoginTooSoonException.class)
    public ResponseEntity<ApiErrorResponse> handleLoginTooSoon(LoginTooSoonException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("TOO_SOON")
                .message(ex.getMessage())
                .remainingSeconds((int) ex.getRetryAfter())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.LOCKED).body(response); // HTTP 423
    }

    @ExceptionHandler(CaptchaRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleCaptchaRequired(CaptchaRequiredException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("CAPTCHA_REQUIRED")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(response); // HTTP 428
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("RATE_LIMIT_EXCEEDED")
                .message(ex.getMessage())
                .remainingSeconds(ex.getRemainingSeconds())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response); // HTTP 429
    }

    // ── 409: Conflict (Duplicate Data) ───────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("EMAIL_ALREADY_EXISTS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePhoneExists(PhoneAlreadyExistsException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("PHONE_ALREADY_EXISTS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ── 404: Domain entity not found ─────────────────────────────────────────

    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Requested resource not found.";
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("RESOURCE_NOT_FOUND")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ── 400: Business rule / bad input ────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("BAD_REQUEST")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 422: Bean Validation failures (@Valid) ────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        String firstMessage = fieldErrors.values().stream()
                .findFirst()
                .orElse("Validation failed");

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("VALIDATION_FAILED")
                .message(firstMessage)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ── 422: Order State Violations ───────────────────────────────────────────

    /**
     * 422 — Order state violations (e.g. returning a non-DELIVERED order).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false).code("INVALID_STATE")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI()).build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ── 500: Last-resort catch-all ────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}