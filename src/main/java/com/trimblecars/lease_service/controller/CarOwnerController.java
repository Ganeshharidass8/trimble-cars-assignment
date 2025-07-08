package com.trimblecars.lease_service.controller;

import com.trimblecars.lease_service.dto.CarRequestDTO;
import com.trimblecars.lease_service.dto.CarResponseDTO;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class CarOwnerController {

    private final CarService carService;

    /**
     * Register a new car under a given owner
     */
    @PostMapping("/{ownerId}/cars")
    public ResponseEntity<ResponseModel<CarResponseDTO>> registerCar(
            @PathVariable Long ownerId,
            @RequestBody CarRequestDTO carDTO) {

        log.info("[Owner] Registering car for owner ID: {}", ownerId);

        ResponseModel<CarResponseDTO> response = carService.registerCar(ownerId, carDTO);

        return ResponseEntity.ok(response);
    }


    /**
     * View all cars owned by a specific owner
     */
    @GetMapping("/{ownerId}/cars")
    public ResponseEntity<ResponseModel<List<CarResponseDTO>>> getCarsByOwner(@PathVariable Long ownerId) {
        log.info("[Owner] Fetching all cars for owner ID: {}", ownerId);
        return ResponseEntity.ok(carService.getCarsByOwner(ownerId));
    }

}
