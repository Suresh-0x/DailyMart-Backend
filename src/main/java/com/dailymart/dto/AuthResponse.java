package com.dailymart.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private String email;
    private String firstName;
    private String lastName;
    private String profileImage;
    private List<String> roles;
}
