package learnMongoDb.learnSpringMongoDb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private boolean success;

    private String code;

    private String message;

    private Integer remainingSeconds;

    // Added to support Inventory / Cart limit errors
    private Integer availableStock;

    private LocalDateTime timestamp;

    private String path;
}