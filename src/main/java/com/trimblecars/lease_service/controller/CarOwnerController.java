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
    public ResponseEntity<ResponseModel<CarResponseDTO>> registerCar(@PathVariable Long ownerId,
                                                                     @RequestBody CarRequestDTO carDTO) {
        log.info("[Owner] Registering car for owner ID: {}", ownerId);

        Car car = new Car();
        car.setModel(carDTO.getModel());

        Car savedCar = carService.registerCar(ownerId, car);

        CarResponseDTO response = new CarResponseDTO();
        response.setId(savedCar.getId());
        response.setModel(savedCar.getModel());
        response.setStatus(savedCar.getStatus().name());
        response.setOwnerEmail(savedCar.getOwner().getEmail());

        return ResponseEntity.ok(ResponseModel.success("Car registered successfully.", response));
    }

    /**
     * View all cars owned by a specific owner
     */
    @GetMapping("/{ownerId}/cars")
    public ResponseEntity<ResponseModel<List<CarResponseDTO>>> getCarsByOwner(@PathVariable Long ownerId) {
        log.info("[Owner] Fetching all cars for owner ID: {}", ownerId);

        List<CarResponseDTO> response = carService.getCarsByOwner(ownerId).stream().map(car -> {
            CarResponseDTO dto = new CarResponseDTO();
            dto.setId(car.getId());
            dto.setModel(car.getModel());
            dto.setStatus(car.getStatus().name());
            dto.setOwnerEmail(car.getOwner().getEmail());
            return dto;
        }).collect(Collectors.toList());

        String message = response.isEmpty()
                ? "No cars found for the given owner."
                : "Owner cars fetched successfully.";

        return ResponseEntity.ok(ResponseModel.success(message, response));
    }
}
