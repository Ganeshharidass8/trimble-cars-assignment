package com.trimblecars.lease_service.controller;

import com.trimblecars.lease_service.dto.*;
import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.model.ResponseModel;
import com.trimblecars.lease_service.service.AdminService;
import com.trimblecars.lease_service.service.LeaseService;
import com.trimblecars.lease_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
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

    // --- USERS ---

    @PostMapping("/users")
    public ResponseEntity<ResponseModel<UserResponseDTO>> registerUser(@RequestBody UserRequestDTO dto) {
        log.info("[Admin] Registering user: {}", dto.getEmail());

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.valueOf(String.valueOf(dto.getRole())));

        User saved = adminService.registerUser(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole().name());

        return ResponseEntity.ok(ResponseModel.success("User registered successfully.", response));
    }

    // --- CARS ---

    @PostMapping("/owners/{ownerId}/cars")
    public ResponseEntity<ResponseModel<CarResponseDTO>> registerCar(@PathVariable Long ownerId, @RequestBody CarRequestDTO dto) {
        log.info("[Admin] Registering car for owner: {}", ownerId);

        Car car = new Car();
        car.setModel(dto.getModel());

        Car saved = adminService.registerCar(ownerId, car);

        CarResponseDTO response = new CarResponseDTO();
        response.setId(saved.getId());
        response.setModel(saved.getModel());
        response.setStatus(saved.getStatus().name());
        response.setOwnerEmail(saved.getOwner().getEmail());

        return ResponseEntity.ok(ResponseModel.success("Car registered successfully.", response));
    }

    @GetMapping("/cars")
    public ResponseEntity<ResponseModel<List<CarResponseDTO>>> getAllCars(@RequestParam(required = false) CarStatus status) {
        List<Car> cars = (status != null) ? adminService.getCarsByStatus(status) : adminService.getAllCars();

        List<CarResponseDTO> response = cars.stream().map(car -> {
            CarResponseDTO dto = new CarResponseDTO();
            dto.setId(car.getId());
            dto.setModel(car.getModel());
            dto.setStatus(car.getStatus().name());
            dto.setOwnerEmail(car.getOwner().getEmail());
            return dto;
        }).collect(Collectors.toList());

        String message = response.isEmpty()
                ? "No cars found for the given status."
                : "Car list fetched successfully.";

        return ResponseEntity.ok(ResponseModel.success(message, response));
    }

    // --- LEASES ---

    @PostMapping("/customers/{customerId}/lease")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> startLease(@PathVariable Long customerId, @RequestBody LeaseRequestDTO dto) {
        log.info("[Admin] Starting lease for customer {} on car {}", customerId, dto.getCarId());

        Lease lease = adminService.startLease(customerId, dto.getCarId());

        LeaseResponseDTO response = new LeaseResponseDTO();
        response.setLeaseId(lease.getId());
        response.setCarModel(lease.getCar().getModel());
        response.setCustomerEmail(lease.getCustomer().getEmail());
        response.setStartDate(lease.getStartDate());
        response.setEndDate(lease.getEndDate());

        return ResponseEntity.ok(ResponseModel.success("Lease started successfully.", response));
    }

    @PostMapping("/leases/{leaseId}/end")
    public ResponseEntity<ResponseModel<LeaseResponseDTO>> endLease(@PathVariable Long leaseId) {
        log.info("[Admin] Ending lease: {}", leaseId);

        Lease lease = adminService.endLease(leaseId);

        LeaseResponseDTO response = new LeaseResponseDTO();
        response.setLeaseId(lease.getId());
        response.setCarModel(lease.getCar().getModel());
        response.setCustomerEmail(lease.getCustomer().getEmail());
        response.setStartDate(lease.getStartDate());
        response.setEndDate(lease.getEndDate());

        return ResponseEntity.ok(ResponseModel.success("Lease ended successfully.", response));
    }

    @GetMapping("/leases/by-customer/{customerId}")
    public ResponseEntity<ResponseModel<List<LeaseResponseDTO>>> getLeasesByCustomer(@PathVariable Long customerId) {
        log.info("[Admin] Fetching leases by customer ID: {}", customerId);

        List<LeaseResponseDTO> response = adminService.getLeasesByCustomer(customerId).stream().map(lease -> {
            LeaseResponseDTO dto = new LeaseResponseDTO();
            dto.setLeaseId(lease.getId());
            dto.setCarModel(lease.getCar().getModel());
            dto.setCustomerEmail(lease.getCustomer().getEmail());
            dto.setStartDate(lease.getStartDate());
            dto.setEndDate(lease.getEndDate());
            return dto;
        }).collect(Collectors.toList());

        String message = response.isEmpty()
                ? "No lease history found for this customer."
                : "Lease history fetched successfully.";

        return ResponseEntity.ok(ResponseModel.success(message, response));
    }

    @GetMapping("/leases/by-car/{carId}")
    public ResponseEntity<ResponseModel<List<LeaseResponseDTO>>> getLeasesByCar(@PathVariable Long carId) {
        log.info("[Admin] Fetching leases by car ID: {}", carId);

        List<LeaseResponseDTO> response = adminService.getLeasesByCar(carId).stream().map(lease -> {
            LeaseResponseDTO dto = new LeaseResponseDTO();
            dto.setLeaseId(lease.getId());
            dto.setCarModel(lease.getCar().getModel());
            dto.setCustomerEmail(lease.getCustomer().getEmail());
            dto.setStartDate(lease.getStartDate());
            dto.setEndDate(lease.getEndDate());
            return dto;
        }).collect(Collectors.toList());

        String message = response.isEmpty()
                ? "No lease history found for this car."
                : "Lease history fetched successfully.";

        return ResponseEntity.ok(ResponseModel.success(message, response));
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
    public ResponseEntity<String> bootstrapTestUsers() {
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

        try {
            userService.registerUsers(testUsers); // ✅ Use AdminService layer
            return ResponseEntity.ok("✅ 11 users inserted (1 Admin, 5 Owners, 5 Customers)");
        } catch (Exception e) {
            log.error("Failed to insert test users", e);
            return ResponseEntity.internalServerError()
                    .body("Error inserting users: " + e.getMessage());
        }
    }


}
