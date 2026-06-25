package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class MessageResponse {
    private String message;
    private boolean success = true;

    public MessageResponse(String message) {
        this.message = message;
    }
}
