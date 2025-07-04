package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
import com.trimblecars.lease_service.repository.CarRepository;
import com.trimblecars.lease_service.repository.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CarService carService;

    @Test
    @DisplayName("✅ Should register car successfully for a valid owner")
    void shouldRegisterCarForOwner() {
        User owner = new User(2L, "Carlos", "carlos@trimble.com", UserRole.OWNER);
        Car car = new Car(null, "Hyundai Creta", CarStatus.IDLE, null);

        when(userService.getUserById(2L)).thenReturn(owner); // ✅ fix
        when(carRepository.save(any())).thenAnswer(invocation -> {
            Car savedCar = invocation.getArgument(0);
            savedCar.setId(1L);
            return savedCar;
        });

        Car result = carService.registerCar(2L, car);

        assertNotNull(result.getId());
        assertEquals("Hyundai Creta", result.getModel());
        assertEquals(owner, result.getOwner());
    }


    @Test
    void shouldThrowIfOwnerNotFound() {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("Owner not found with ID: 99"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> carService.registerCar(99L, new Car())
        );

        assertEquals("Owner not found with ID: 99", ex.getMessage());
    }


    @Test
    void shouldReturnCarsByOwnerId() {
        List<Car> mockList = List.of(new Car(1L, "Tata Nexon", CarStatus.IDLE, new User()));
        when(carRepository.findByOwnerId(2L)).thenReturn(mockList);

        List<Car> result = carService.getCarsByOwner(2L);
        assertEquals(1, result.size());
        assertEquals("Tata Nexon", result.get(0).getModel());
    }

    @Test
    void shouldReturnCarsByStatus() {
        when(carRepository.findByStatus(CarStatus.IDLE)).thenReturn(List.of(new Car(1L, "Honda Civic", CarStatus.IDLE, null)));
        List<Car> result = carService.getCarsByStatus(CarStatus.IDLE);
        assertEquals(1, result.size());
        assertEquals(CarStatus.IDLE, result.get(0).getStatus());
    }
}
