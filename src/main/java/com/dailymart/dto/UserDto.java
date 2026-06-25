package com.dailymart.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImage;
    private boolean enabled;
    private List<String> roles;
    private LocalDateTime createdAt;
}
