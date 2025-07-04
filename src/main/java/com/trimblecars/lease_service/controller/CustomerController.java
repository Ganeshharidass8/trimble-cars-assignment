package com.trimblecars.lease_service.controller;

import com.trimblecars.lease_service.dto.LeaseRequestDTO;
import com.trimblecars.lease_service.dto.LeaseResponseDTO;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.service.CarService;
import com.trimblecars.lease_service.service.LeaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CarService carService;
    private final LeaseService leaseService;

    /**
     * View available cars for leasing (only IDLE ones)
     */
    @GetMapping("/cars")
    public ResponseEntity<ResponseModel<List<Car>>> getAvailableCars() {
        log.info("[Customer] Fetching all IDLE cars available for leasing.");
        List<Car> cars = carService.getCarsByStatus(CarStatus.IDLE);
        String message = cars.isEmpty() ? "No IDLE cars available." : "Idle cars fetched successfully.";
        return ResponseEntity.ok(ResponseModel.success(message, cars));
    }

    /**
     * Start lease for a customer and car
     */
    @PostMapping("/{customerId}/lease")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> startLease(@PathVariable Long customerId,
                                                                      @RequestBody LeaseRequestDTO dto) {
        log.info("[Customer] Starting lease for customer {} and car {}", customerId, dto.getCarId());
        Lease lease = leaseService.startLease(customerId, dto.getCarId());

        LeaseResponseDTO response = new LeaseResponseDTO();
        response.setLeaseId(lease.getId());
        response.setCarModel(lease.getCar().getModel());
        response.setCustomerEmail(lease.getCustomer().getEmail());
        response.setStartDate(lease.getStartDate());
        response.setEndDate(lease.getEndDate());

        return ResponseEntity.ok(ResponseModel.success("Lease started successfully.", response));
    }

    /**
     * End an active lease for a customer
     */
    @PostMapping("/{customerId}/lease/{leaseId}/end")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> endLease(@PathVariable Long customerId,
                                                                    @PathVariable Long leaseId) {
        log.info("[Customer] Ending lease ID {} for customer {}", leaseId, customerId);
        Lease lease = leaseService.endLease(leaseId);

        LeaseResponseDTO response = new LeaseResponseDTO();
        response.setLeaseId(lease.getId());
        response.setCarModel(lease.getCar().getModel());
        response.setCustomerEmail(lease.getCustomer().getEmail());
        response.setStartDate(lease.getStartDate());
        response.setEndDate(lease.getEndDate());

        return ResponseEntity.ok(ResponseModel.success("Lease ended successfully.", response));
    }

    /**
     * View customer's lease history
     */
    @GetMapping("/{customerId}/leases")
    public ResponseEntity<ResponseModel<List<LeaseResponseDTO>>> getLeaseHistory(@PathVariable Long customerId) {
        log.info("[Customer] Fetching lease history for customer ID: {}", customerId);

        List<LeaseResponseDTO> response = leaseService.getLeasesByCustomer(customerId).stream().map(lease -> {
            LeaseResponseDTO dto = new LeaseResponseDTO();
            dto.setLeaseId(lease.getId());
            dto.setCarModel(lease.getCar().getModel());
            dto.setCustomerEmail(lease.getCustomer().getEmail());
            dto.setStartDate(lease.getStartDate());
            dto.setEndDate(lease.getEndDate());
            return dto;
        }).collect(Collectors.toList());

        String message = response.isEmpty() ? "No lease history found for the customer." : "Lease history fetched successfully.";
        return ResponseEntity.ok(ResponseModel.success(message, response));
    }
}
