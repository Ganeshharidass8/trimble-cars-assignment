package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
import com.trimblecars.lease_service.repository.CarRepository;
import com.trimblecars.lease_service.repository.LeaseRepository;
import com.trimblecars.lease_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaseServiceTest {

    @Mock private CarRepository carRepository;
    @Mock private LeaseRepository leaseRepository;
    @Mock private UserService userService;
    @InjectMocks private LeaseService leaseService;

    private User customer;
    private Car car;

    @BeforeEach
    void setup() {
        customer = new User(7L, "Rajesh", "rajesh@trimble.com", UserRole.CUSTOMER);
        car = new Car(4L, "Tesla Model 3", CarStatus.IDLE, null);
    }

    @Test
    void shouldStartLeaseSuccessfully() {
        Lease lease = new Lease(null, car, customer, LocalDate.now(), null);

        when(userService.getUserById(customer.getId())).thenReturn(customer);  // ✅ Fix this
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(leaseRepository.countByCustomerIdAndEndDateIsNull(customer.getId())).thenReturn(0L);
        when(leaseRepository.save(any(Lease.class))).thenReturn(lease);

        Lease result = leaseService.startLease(customer.getId(), car.getId());

        assertEquals(customer, result.getCustomer());
        assertEquals(car, result.getCar());
        assertEquals(LocalDate.now(), result.getStartDate());
        assertNull(result.getEndDate());
    }

    @Test
    void shouldNotStartLeaseIfLimitExceeded() {
        when(userService.getUserById(customer.getId())).thenReturn(customer);  // ✅ Fix
        when(leaseRepository.countByCustomerIdAndEndDateIsNull(customer.getId())).thenReturn(2L);

        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> leaseService.startLease(customer.getId(), car.getId())
        );

        assertEquals("Customer already has 2 active leases.", ex.getMessage());
    }

    @Test
    void shouldNotStartLeaseIfCarNotIdle() {
        car.setStatus(CarStatus.ON_LEASE);

        when(userService.getUserById(customer.getId())).thenReturn(customer);
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(leaseRepository.countByCustomerIdAndEndDateIsNull(customer.getId())).thenReturn(0L);

        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> leaseService.startLease(customer.getId(), car.getId())
        );

        assertEquals("Car is not available for lease.", ex.getMessage());
    }

    @Test
    void shouldEndLeaseSuccessfully() {
        car.setStatus(CarStatus.ON_LEASE);
        Lease lease = new Lease(1L, car, customer, LocalDate.now().minusDays(3), null);

        when(leaseRepository.findById(1L)).thenReturn(Optional.of(lease));
        when(leaseRepository.save(any(Lease.class))).thenReturn(lease);

        Lease ended = leaseService.endLease(1L);

        assertNotNull(ended.getEndDate());
        assertEquals(CarStatus.IDLE, ended.getCar().getStatus());
    }

    @Test
    void shouldThrowIfLeaseNotFound() {
        when(leaseRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> leaseService.endLease(999L)
        );

        assertEquals("Lease not found with ID: 999", ex.getMessage());
    }
}
