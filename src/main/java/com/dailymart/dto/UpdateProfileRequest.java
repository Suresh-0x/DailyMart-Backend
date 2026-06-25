package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
}
