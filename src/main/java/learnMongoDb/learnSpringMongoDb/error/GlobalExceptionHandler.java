package learnMongoDb.learnSpringMongoDb.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * GlobalExceptionHandler
 *
 * Centralises all error responses so every exception surfaces as clean JSON
 * rather than a raw Spring error page or stack trace.
 *
 * HTTP status contract:
 *   404 Not Found              — UserNotFoundException, NoSuchElementException
 *   400 Bad Request            — RuntimeException (business rule violations)
 *   422 Unprocessable Entity   — @Valid constraint failures
 *   500 Internal Server Error  — any unexpected Exception (last-resort handler)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404: Domain entity not found ─────────────────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorBody(ex.getMessage()));
    }

    /**
     * Catches Optional.get() misuse and repository lookups that return empty.
     * Always prefer throwing a domain-specific exception (like UserNotFoundException)
     * from the service layer instead of letting this bubble up.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElement(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorBody(ex.getMessage() != null ? ex.getMessage() : "Requested resource not found."));
    }

    // ── 400: Business rule / bad input ────────────────────────────────────────

    /**
     * Catches all RuntimeExceptions not handled above (e.g. invalid login,
     * duplicate email, stock exhausted). The service layer throws these with
     * a user-facing message.
     *
     * Note: IllegalArgumentException IS a RuntimeException, but is listed
     * separately so its intent (caller error, 400) is explicit.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ex.getMessage()));
    }

    // ── 422: Bean Validation failures (@Valid) ────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // LinkedHashMap preserves insertion order so the first field error
        // appears first — useful for the frontend to display in form order.
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // putIfAbsent: keeps the first (most specific) message per field.
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        String firstMessage = fieldErrors.values().stream()
                .findFirst()
                .orElse("Validation failed");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",  firstMessage);
        body.put("errors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    // ── 500: Last-resort catch-all ────────────────────────────────────────────

    /**
     * Catches any non-Runtime checked exception or unexpected error that
     * slipped through. Returns 500 with a generic message — never expose
     * internal exception details to the client in production.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        // Log the full stack trace server-side (replace with your logger if available)
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("An unexpected error occurred. Please try again later."));
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }
}