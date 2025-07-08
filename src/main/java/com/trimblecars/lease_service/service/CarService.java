package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.dto.CarRequestDTO;
import com.trimblecars.lease_service.dto.CarResponseDTO;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final UserService userService;

    public ResponseModel<CarResponseDTO> registerCar(Long ownerId, CarRequestDTO dto) {
        log.info("Registering new car for owner ID: {}", ownerId);

        if (ownerId == null || dto == null) {
            throw new IllegalArgumentException("Owner ID and car details must not be null.");
        }

        User owner = userService.getUserById(ownerId); // Will throw ResourceNotFoundException if invalid

        if (owner.getRole() != UserRole.OWNER) {
            throw new BusinessRuleViolationException("User must be an OWNER to register a car.");
        }

        Car car = mapToCarEntity(dto, owner);
        Car savedCar = carRepository.save(car);

        log.info("Car registered successfully: {} (Owner: {})", savedCar.getModel(), owner.getEmail());

        CarResponseDTO responseDTO = mapToCarResponseDTO(savedCar);
        return ResponseModel.success("Car registered successfully.", responseDTO);
    }

    public ResponseModel<List<CarResponseDTO>> getAllCarsResponse(CarStatus status) {
        return (status != null) ? getCarsByStatus(status) : getAllCars();
    }

    public ResponseModel<List<CarResponseDTO>> getCarsByStatus(CarStatus status) {
        log.info("Fetching cars with status: {}", status);
        List<Car> cars = carRepository.findByStatus(status);

        List<CarResponseDTO> dtos = cars.stream()
                .map(this::mapToCarResponseDTO)
                .toList();

        String message = dtos.isEmpty()
                ? "No cars found with status: " + status
                : "Cars fetched successfully";

        return ResponseModel.success(message, dtos);
    }

    public ResponseModel<List<CarResponseDTO>> getAllCars() {
        log.info("Fetching all cars (no status filter)");
        List<Car> cars = carRepository.findAll();

        List<CarResponseDTO> dtos = cars.stream()
                .map(this::mapToCarResponseDTO)
                .toList();

        String message = dtos.isEmpty()
                ? "No cars found in system"
                : "All cars fetched successfully";

        return ResponseModel.success(message, dtos);
    }

    public ResponseModel<List<CarResponseDTO>> getCarsByOwner(Long ownerId) {
        log.info("Fetching cars for owner ID: {}", ownerId);
        List<Car> cars = carRepository.findByOwnerId(ownerId);

        List<CarResponseDTO> dtos = cars.stream()
                .map(this::mapToCarResponseDTO)
                .toList();

        String message = dtos.isEmpty()
                ? "No cars found for the given owner."
                : "Owner cars fetched successfully.";

        return ResponseModel.success(message, dtos);
    }


    // -------- Mapping Helpers --------
    private Car mapToCarEntity(CarRequestDTO dto, User owner) {
        Car car = new Car();
        car.setModel(dto.getModel());
        car.setStatus(CarStatus.IDLE); // Default status
        car.setOwner(owner);
        return car;
    }

    private CarResponseDTO mapToCarResponseDTO(Car car) {
        CarResponseDTO dto = new CarResponseDTO();
        dto.setId(car.getId());
        dto.setModel(car.getModel());
        dto.setStatus(car.getStatus().name());
        dto.setOwnerEmail(car.getOwner().getEmail());
        return dto;
    }
}
