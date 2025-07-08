package com.trimblecars.lease_service.controller;

import com.trimblecars.lease_service.dto.*;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.service.AdminService;
import com.trimblecars.lease_service.service.CarService;
import com.trimblecars.lease_service.service.LeaseService;
import com.trimblecars.lease_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final LeaseService leaseService;

    private final UserService userService;

    private final CarService carService;

    // --- USERS ---

    @PostMapping("/users")
    public ResponseEntity<ResponseModel<UserResponseDTO>> registerUser(@RequestBody UserRequestDTO dto) {
        return adminService.registerUser(dto);
    }


    // --- CARS ---

    @PostMapping("/owners/{ownerId}/cars")
    public ResponseEntity<ResponseModel<CarResponseDTO>> registerCar(@PathVariable Long ownerId,
                                                                     @RequestBody CarRequestDTO dto) {
        log.info("[Admin] Registering car for owner: {}", ownerId);
        return ResponseEntity.ok(carService.registerCar(ownerId, dto));
    }

    @GetMapping("/cars")
    public ResponseEntity<ResponseModel<List<CarResponseDTO>>> getAllCars(
            @RequestParam(required = false) CarStatus status) {

        ResponseModel<List<CarResponseDTO>> response = carService.getAllCarsResponse(status);

        return ResponseEntity.ok(response);
    }

    // --- LEASES ---

    @PostMapping("/customers/{customerId}/lease")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> startLease(@PathVariable Long customerId, @RequestBody LeaseRequestDTO dto) {
        log.info("[Admin] Starting lease for customer {} on car {}", customerId, dto.getCarId());

        return ResponseEntity.ok(leaseService.startLease(customerId, dto.getCarId()));
    }

    @PostMapping("/leases/{leaseId}/end")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> endLease(@PathVariable Long leaseId) {
        log.info("[Admin] Ending lease: {}", leaseId);

        return ResponseEntity.ok(leaseService.endLease(leaseId));
    }

    @GetMapping("/leases/by-customer/{customerId}")
    public ResponseEntity<ResponseModel<List<LeaseResponseDTO>>> getLeasesByCustomer(@PathVariable Long customerId) {
        log.info("[Admin] Fetching leases by customer ID: {}", customerId);
        return ResponseEntity.ok(leaseService.getLeasesByCustomer(customerId));
    }

    @GetMapping("/leases/by-car/{carId}")
    public ResponseEntity<ResponseModel<List<LeaseResponseDTO>>> getLeasesByCar(@PathVariable Long carId) {
        log.info("[Admin] Fetching leases by car ID: {}", carId);
        return ResponseEntity.ok(leaseService.getLeasesByCar(carId));
    }

    @GetMapping("/leases/export")
    public void exportLeases(@RequestParam(defaultValue = "csv") String format, HttpServletResponse response) throws IOException {
        List<Lease> leases = leaseService.getAllLeases();

        if (format.equalsIgnoreCase("pdf")) {
            adminService.exportAsPdf(leases, response);
        } else {
            adminService.exportAsCsv(response);
        }
    }

    @PostMapping("/bootstrap-users")
    public ResponseEntity<String> bootstrapTestUsersAndCars() {
        List<User> testUsers = List.of(
                new User(null, "Admin1", "admin@trimble.com", UserRole.ADMIN),
                new User(null, "Carlos", "carlos@trimble.com", UserRole.OWNER),
                new User(null, "Ayesha", "ayesha@trimble.com", UserRole.OWNER),
                new User(null, "Daniel", "daniel@trimble.com", UserRole.OWNER),
                new User(null, "Ravi", "ravi@trimble.com", UserRole.OWNER),
                new User(null, "Sofia", "sofia@trimble.com", UserRole.OWNER),
                new User(null, "Rajesh", "rajesh@trimble.com", UserRole.CUSTOMER),
                new User(null, "Emily", "emily@trimble.com", UserRole.CUSTOMER),
                new User(null, "Ali", "ali@trimble.com", UserRole.CUSTOMER),
                new User(null, "Lina", "lina@trimble.com", UserRole.CUSTOMER),
                new User(null, "Sundar", "sundar@trimble.com", UserRole.CUSTOMER)
        );

        int insertedUsers = 0;
        int carsRegistered = 0;

        for (User user : testUsers) {
            try {
                User saved = userService.registerUser(user); // throws if already exists
                insertedUsers++;

                if (saved.getRole() == UserRole.OWNER) {
                    carService.registerCar(saved.getId(), new CarRequestDTO("Honda City - "));
                    carService.registerCar(saved.getId(), new CarRequestDTO("Tata Nexon - "));
                    carsRegistered += 2;
                }

            } catch (Exception e) {
                log.warn("Skipping existing or invalid user: {} ({})", user.getEmail(), e.getMessage());
            }
        }

        String message = String.format("✅ %d users registered, %d cars assigned to owners", insertedUsers, carsRegistered);
        return ResponseEntity.ok(message);
    }

}
