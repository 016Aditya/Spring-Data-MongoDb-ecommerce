package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Address {
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
