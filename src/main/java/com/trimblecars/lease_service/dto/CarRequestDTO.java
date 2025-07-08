package com.trimblecars.lease_service.dto;

import com.trimblecars.lease_service.enums.CarStatus;
import lombok.Data;

@Data
public class CarRequestDTO {
    private String model;

    public CarRequestDTO(String model){
        this.model = model;
    }

    public CarRequestDTO(){

    }
}
