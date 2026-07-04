package learnMongoDb.learnSpringMongoDb.dto.response;

import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UserDto user;
}