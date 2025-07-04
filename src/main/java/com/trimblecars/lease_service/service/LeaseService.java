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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final CarRepository carRepository;
    private final UserService userService;

    /**
     * Starts a lease for a customer and car
     */
    @Transactional
    public Lease startLease(Long customerId, Long carId) {
        if (customerId == null || carId == null) {
            throw new IllegalArgumentException("Customer ID and Car ID must not be null.");
        }

        log.info("Starting lease - customerId={}, carId={}", customerId, carId);

        // Get Customer
        User customer = userService.getUserById(customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new BusinessRuleViolationException("Only CUSTOMERS can start leases.");
        }

        // Check active leases
        long activeLeases = leaseRepository.countByCustomerIdAndEndDateIsNull(customerId);
        if (activeLeases >= 2) {
            throw new BusinessRuleViolationException("Customer already has 2 active leases.");
        }

        // Get Car
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + carId));

        if (car == null || car.getStatus() == null) {
            throw new BusinessRuleViolationException("Car data is corrupted or status is missing.");
        }

        if (car.getStatus() != CarStatus.IDLE) {
            throw new BusinessRuleViolationException("Car is not available for lease.");
        }

        // Update car status
        car.setStatus(CarStatus.ON_LEASE);
        carRepository.save(car);

        // Create lease entry
        Lease lease = new Lease();
        lease.setCar(car);
        lease.setCustomer(customer);
        lease.setStartDate(LocalDate.now());
        lease.setEndDate(null);

        Lease saved = leaseRepository.save(lease);
        log.info("Lease started successfully. Lease ID: {}", saved.getId());
        return saved;
    }


    /**
     * Ends an existing lease
     */
    @Transactional
    public Lease endLease(Long leaseId) {
        log.info("Ending lease with ID: {}", leaseId);

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with ID: " + leaseId));

        if (lease.getEndDate() != null) {
            throw new BusinessRuleViolationException("Lease already ended.");
        }

        lease.setEndDate(LocalDate.now());

        // Reset car status to IDLE
        Car car = lease.getCar();
        car.setStatus(CarStatus.IDLE);
        carRepository.save(car);

        Lease saved = leaseRepository.save(lease);
        log.info("Lease ended successfully. Lease ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get all leases for a specific customer
     */
    public List<Lease> getLeasesByCustomer(Long customerId) {
        log.info("Fetching lease history for customer ID: {}", customerId);
        return leaseRepository.findByCustomerId(customerId);
    }

    /**
     * Get all leases for a specific car
     */
    public List<Lease> getLeasesByCar(Long carId) {
        log.info("Fetching lease history for car ID: {}", carId);
        return leaseRepository.findByCarId(carId);
    }

    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

}
