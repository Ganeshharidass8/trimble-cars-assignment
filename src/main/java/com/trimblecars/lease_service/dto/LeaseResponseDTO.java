package com.trimblecars.lease_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaseResponseDTO {
    private Long leaseId;
    private String carModel;
    private String customerEmail;
    private LocalDate startDate;
    private LocalDate endDate;
}
