package learnMongoDb.learnSpringMongoDb.error;

import learnMongoDb.learnSpringMongoDb.dto.response.ApiErrorResponse;
import learnMongoDb.learnSpringMongoDb.error.EmailAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.InvalidCredentialsException;
import learnMongoDb.learnSpringMongoDb.error.PhoneAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 409: Conflict (Duplicate Data) ───────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        ApiErrorResponse response = new ApiErrorResponse("EMAIL_ALREADY_EXISTS", ex.getMessage());
        response.setField("email");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePhoneExists(PhoneAlreadyExistsException ex) {
        ApiErrorResponse response = new ApiErrorResponse("PHONE_ALREADY_EXISTS", ex.getMessage());
        response.setField("phoneNumber");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ── 401: Unauthorized (Login Failures) ───────────────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiErrorResponse response = new ApiErrorResponse("INVALID_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ── 404: Domain entity not found ─────────────────────────────────────────

    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Requested resource not found.";
        ApiErrorResponse response = new ApiErrorResponse("RESOURCE_NOT_FOUND", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ── 400: Business rule / bad input ────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorResponse response = new ApiErrorResponse("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 422: Bean Validation failures (@Valid) ────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        String firstMessage = fieldErrors.values().stream()
                .findFirst()
                .orElse("Validation failed");

        ApiErrorResponse response = new ApiErrorResponse("VALIDATION_FAILED", firstMessage);
        response.setErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ── 500: Last-resort catch-all ────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        ex.printStackTrace();
        ApiErrorResponse response = new ApiErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}