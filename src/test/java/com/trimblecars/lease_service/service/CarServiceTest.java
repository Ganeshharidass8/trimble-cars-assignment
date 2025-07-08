package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.dto.CarRequestDTO;
import com.trimblecars.lease_service.dto.CarResponseDTO;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.repository.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CarService carService;

    @Test
    @DisplayName("✅ Should register car successfully for a valid OWNER")
    void shouldRegisterCarForOwner() {
        Long ownerId = 2L;
        CarRequestDTO dto = new CarRequestDTO("Hyundai Creta");

        User owner = new User(ownerId, "Carlos", "carlos@trimble.com", UserRole.OWNER);
        Car savedCar = new Car(1L, "Hyundai Creta", CarStatus.IDLE, owner);

        when(userService.getUserById(ownerId)).thenReturn(owner);
        when(carRepository.save(any(Car.class))).thenReturn(savedCar);

        ResponseModel<CarResponseDTO> result = carService.registerCar(ownerId, dto);

        assertNotNull(result);
        assertEquals("Car registered successfully.", result.getMessage());
        assertEquals("Hyundai Creta", result.getData().getModel());
        assertEquals("IDLE", result.getData().getStatus());
        assertEquals("carlos@trimble.com", result.getData().getOwnerEmail());
    }

    @Test
    @DisplayName("❌ Should throw ResourceNotFoundException if owner not found")
    void shouldThrowIfOwnerNotFound() {
        Long invalidOwnerId = 99L;
        when(userService.getUserById(invalidOwnerId)).thenThrow(new ResourceNotFoundException("Owner not found with ID: 99"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> carService.registerCar(invalidOwnerId, new CarRequestDTO("Tata Harrier"))
        );

        assertEquals("Owner not found with ID: 99", ex.getMessage());
    }

    @Test
    @DisplayName("❌ Should throw BusinessRuleViolationException if user is not OWNER")
    void shouldThrowIfUserNotOwner() {
        Long customerId = 3L;
        User customer = new User(customerId, "Rajesh", "rajesh@trimble.com", UserRole.CUSTOMER);

        when(userService.getUserById(customerId)).thenReturn(customer);

        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> carService.registerCar(customerId, new CarRequestDTO("Kia Sonet"))
        );

        assertEquals("User must be an OWNER to register a car.", ex.getMessage());
    }

    @Test
    @DisplayName("✅ Should return list of cars by owner")
    void shouldReturnCarsByOwnerId() {
        User owner = new User(2L, "Carlos", "carlos@trimble.com", UserRole.OWNER);
        List<Car> mockList = List.of(new Car(1L, "Tata Nexon", CarStatus.IDLE, owner));

        when(carRepository.findByOwnerId(2L)).thenReturn(mockList);

        ResponseModel<List<CarResponseDTO>> response = carService.getCarsByOwner(2L);

        assertEquals(1, response.getData().size());
        assertEquals("Tata Nexon", response.getData().get(0).getModel());
    }


    @Test
    @DisplayName("✅ Should return cars by status")
    void shouldReturnCarsByStatus() {
        User owner = new User(2L, "Carlos", "carlos@trimble.com", UserRole.OWNER);
        Car car = new Car(1L, "Honda Civic", CarStatus.IDLE, owner);

        when(carRepository.findByStatus(CarStatus.IDLE)).thenReturn(List.of(car));

        ResponseModel<List<CarResponseDTO>> response = carService.getCarsByStatus(CarStatus.IDLE);
        List<CarResponseDTO> result = response.getData();

        assertEquals(1, result.size());
        assertEquals("Honda Civic", result.get(0).getModel());
        assertEquals("carlos@trimble.com", result.get(0).getOwnerEmail());
        assertEquals("IDLE", result.get(0).getStatus());
    }

}
