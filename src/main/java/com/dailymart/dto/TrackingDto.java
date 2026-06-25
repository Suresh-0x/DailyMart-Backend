package com.dailymart.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingDto {
    private String status;
    private String description;
    private String location;
    private LocalDateTime timestamp;
}
