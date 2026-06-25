package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank String currentPassword;
    @NotBlank @Size(min = 8) String newPassword;
    @NotBlank String confirmPassword;
}
