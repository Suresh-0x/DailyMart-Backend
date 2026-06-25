package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateAddressRequest {
    private String addressType = "HOME";
    @NotBlank String fullName;
    @Pattern(regexp = "^[6-9]\\d{9}$") String phone;
    @NotBlank String addressLine1;
    String addressLine2;
    @NotBlank String city;
    @NotBlank String state;
    @Pattern(regexp = "^\\d{6}$") String pincode;
    String country = "India";
    boolean isDefault;
}
