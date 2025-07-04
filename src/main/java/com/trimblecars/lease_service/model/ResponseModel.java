package com.trimblecars.lease_service.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseModel<T> {
    private String status;       // "SUCCESS" / "FAILURE"
    private String message;      // Human-readable
    private T data;              // Generic payload
    private LocalDateTime timestamp = LocalDateTime.now();

    public ResponseModel(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ResponseModel<T> success(String message, T data) {
        return new ResponseModel<>("SUCCESS", message, data);
    }

    public static <T> ResponseModel<T> failure(String message, T data) {
        return new ResponseModel<>("FAILURE", message, data);
    }
}
