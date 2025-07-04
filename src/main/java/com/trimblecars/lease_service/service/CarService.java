package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
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

    public Car registerCar(Long ownerId, Car car) {
        log.info("Registering new car for owner ID: {}", ownerId);

        if (ownerId == null || car == null) {
            throw new IllegalArgumentException("Owner ID and Car must not be null.");
        }

        User owner = userService.getUserById(ownerId);

        if (owner == null) {
            throw new ResourceNotFoundException("Owner not found with ID: " + ownerId);
        }

        if (owner.getRole() != UserRole.OWNER) {
            throw new BusinessRuleViolationException("User must be an OWNER to register a car.");
        }

        car.setOwner(owner);
        car.setStatus(CarStatus.IDLE);

        Car savedCar = carRepository.save(car);
        log.info("Car registered successfully: {} (Owner: {})", savedCar.getModel(), owner.getEmail());
        return savedCar;
    }


    public List<Car> getCarsByStatus(CarStatus status) {
        log.info("Fetching cars with status: {}", status);
        return carRepository.findByStatus(status);
    }

    public List<Car> getAllCars() {
        log.info("Fetching all cars (no status filter)");
        return carRepository.findAll();
    }


    public List<Car> getCarsByOwner(Long ownerId) {
        log.info("Fetching cars for owner ID: {}", ownerId);
        return carRepository.findByOwnerId(ownerId);
    }
}
