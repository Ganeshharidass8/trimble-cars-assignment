package com.trimblecars.lease_service.dto;

import lombok.Data;

@Data
public class CarResponseDTO {
    private Long id;
    private String model;
    private String status;
    private String ownerEmail;
}
