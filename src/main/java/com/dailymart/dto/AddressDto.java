package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressDto {
    private Long id;
    private String addressType;
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private boolean isDefault;
}
