package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
    private String line1;
    private String city;
    private String state;
    private String zipCode;
    private String country;


}
