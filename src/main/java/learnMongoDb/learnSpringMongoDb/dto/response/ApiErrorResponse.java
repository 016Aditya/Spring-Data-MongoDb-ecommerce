package learnMongoDb.learnSpringMongoDb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private boolean success;

    private String code;

    private String message;

    private Integer remainingSeconds;

    private LocalDateTime timestamp;

    private String path;
}