package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.dto.LeaseResponseDTO;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.exception.BusinessRuleViolationException;
import com.trimblecars.lease_service.exception.ResourceNotFoundException;
import com.trimblecars.lease_service.model.ResponseModel;
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
    public ResponseModel<LeaseResponseDTO> startLease(Long customerId, Long carId) {
        if (customerId == null || carId == null) {
            throw new IllegalArgumentException("Customer ID and Car ID must not be null.");
        }

        log.info("Starting lease - customerId={}, carId={}", customerId, carId);

        User customer = userService.getUserById(customerId);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new BusinessRuleViolationException("Only CUSTOMERS can start leases.");
        }

        long activeLeases = leaseRepository.countByCustomerIdAndEndDateIsNull(customerId);
        if (activeLeases >= 2) {
            throw new BusinessRuleViolationException("Customer already has 2 active leases.");
        }

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + carId));

        if (car.getStatus() == null || car.getStatus() != CarStatus.IDLE) {
            throw new BusinessRuleViolationException("Car is not available for lease.");
        }

        car.setStatus(CarStatus.ON_LEASE);
        carRepository.save(car);

        Lease lease = new Lease();
        lease.setCar(car);
        lease.setCustomer(customer);
        lease.setStartDate(LocalDate.now());

        Lease saved = leaseRepository.save(lease);
        log.info("Lease started successfully. Lease ID: {}", saved.getId());

        LeaseResponseDTO response = mapToLeaseResponseDTO(saved);
        return ResponseModel.success("Lease started successfully.", response);
    }



    /**
     * Ends an existing lease
     */
    @Transactional
    public ResponseModel<LeaseResponseDTO> endLease(Long leaseId) {
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

        LeaseResponseDTO responseDTO = mapToLeaseResponseDTO(saved);
        return ResponseModel.success("Lease ended successfully.", responseDTO);
    }


    @Transactional
    public ResponseModel<LeaseResponseDTO> endLease(Long customerId, Long leaseId) {
        log.info("Attempting to end lease ID {} for customer ID {}", leaseId, customerId);

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + leaseId));

        if (!lease.getCustomer().getId().equals(customerId)) {
//            throw new BusinessRuleViolationException("You can only end your own lease.");
            return ResponseModel.failure("You can only end your own lease.", null);
        }

        lease.setEndDate(LocalDate.now());
        lease.getCar().setStatus(CarStatus.IDLE);
        leaseRepository.save(lease);

        log.info("Lease ID {} successfully ended by customer ID {}", leaseId, customerId);
        LeaseResponseDTO responseDTO = mapToLeaseResponseDTO(lease);
        return ResponseModel.success("Lease ended successfully.", responseDTO);
    }

    /**
     * Get all leases for a specific customer
     */
    public ResponseModel<List<LeaseResponseDTO>> getLeasesByCustomer(Long customerId) {
        log.info("Fetching lease history for customer ID: {}", customerId);

        List<Lease> leases = leaseRepository.findByCustomerId(customerId);

        List<LeaseResponseDTO> dtos = leases.stream()
                .map(this::mapToLeaseResponseDTO)
                .toList();

        String message = dtos.isEmpty()
                ? "No lease history found for the customer."
                : "Lease history fetched successfully.";

        return ResponseModel.success(message, dtos);
    }

    /**
     * Get all leases for a specific car
     */
    public ResponseModel<List<LeaseResponseDTO>> getLeasesByCar(Long carId) {
        log.info("Fetching lease history for car ID: {}", carId);

        List<Lease> leases = leaseRepository.findByCarId(carId);

        List<LeaseResponseDTO> dtos = leases.stream()
                .map(this::mapToLeaseResponseDTO)
                .toList();

        String message = dtos.isEmpty()
                ? "No lease history found for this car."
                : "Lease history fetched successfully.";

        return ResponseModel.success(message, dtos);
    }


    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    private LeaseResponseDTO mapToLeaseResponseDTO(Lease lease) {
        LeaseResponseDTO dto = new LeaseResponseDTO();
        dto.setLeaseId(lease.getId());
        dto.setCarModel(lease.getCar().getModel());
        dto.setCustomerEmail(lease.getCustomer().getEmail());
        dto.setStartDate(lease.getStartDate());
        dto.setEndDate(lease.getEndDate());
        return dto;
    }

}
