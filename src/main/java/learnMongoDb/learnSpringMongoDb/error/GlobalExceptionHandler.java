package learnMongoDb.learnSpringMongoDb.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * Root cause fix:
 * MethodArgumentNotValidException (Bean Validation failures triggered by @Valid)
 * was not handled. Spring Boot's default behaviour returned a 500 HTML
 * Whitelabel Error page. The frontend api.js interceptor reads
 * error.response.data.message || error.response.data.error — neither key
 * exists in an HTML body, so every validation failure showed as
 * "Something went wrong" with no field-level detail.
 *
 * Fix: handle MethodArgumentNotValidException explicitly, collect all
 * field errors into a JSON map, and return 422 Unprocessable Entity.
 * The top-level "error" key mirrors the existing RuntimeException handler
 * so the frontend interceptor works without changes.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Domain / business logic errors (RuntimeException).
     * Examples: "No account found", "Incorrect password", "Email already in use".
     * Returns 400 Bad Request with { "error": "message" }.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bean Validation failures (@Valid on @RequestBody).
     * Examples: "Password must be at least 8 characters", "Phone number must be 10 digits".
     *
     * Returns 422 Unprocessable Entity with:
     *   {
     *     "error":  "<first field message>",   <- read by frontend api.js interceptor
     *     "errors": { "fieldName": "message", ... }
     *   }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        String firstMessage = fieldErrors.values().stream()
                .findFirst()
                .orElse("Validation failed");

        Map<String, Object> body = new HashMap<>();
        body.put("error",  firstMessage);
        body.put("errors", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
