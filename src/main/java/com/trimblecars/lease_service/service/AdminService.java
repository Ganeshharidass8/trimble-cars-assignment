package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.CarStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final CarService carService;
    private final LeaseService leaseService;

    // --- USER MANAGEMENT ---

    public User registerUser(User user) {
        log.info("[Admin] Registering user: {}", user.getEmail());
        return userService.registerUser(user);
    }

    public User getUserById(Long id) {
        log.info("[Admin] Fetching user by ID: {}", id);
        return userService.getUserById(id);
    }

    // --- CAR MANAGEMENT ---

    public Car registerCar(Long ownerId, Car car) {
        log.info("[Admin] Registering car for owner ID: {}", ownerId);
        return carService.registerCar(ownerId, car);
    }

    public List<Car> getAllCars() {
        log.info("[Admin] Fetching all cars");
        return carService.getAllCars(); // or write a new method to fetch all
    }

    public List<Car> getCarsByStatus(CarStatus status) {
        log.info("[Admin] Fetching cars by status: {}", status);
        return carService.getCarsByStatus(status);
    }


    public List<Car> getCarsByOwner(Long ownerId) {
        log.info("[Admin] Fetching cars for owner ID: {}", ownerId);
        return carService.getCarsByOwner(ownerId);
    }

    // --- LEASE MANAGEMENT ---

    public Lease startLease(Long customerId, Long carId) {
        log.info("[Admin] Starting lease for customer {} on car {}", customerId, carId);
        return leaseService.startLease(customerId, carId);
    }

    public Lease endLease(Long leaseId) {
        log.info("[Admin] Ending lease with ID: {}", leaseId);
        return leaseService.endLease(leaseId);
    }

    public List<Lease> getLeasesByCustomer(Long customerId) {
        log.info("[Admin] Fetching leases for customer ID: {}", customerId);
        return leaseService.getLeasesByCustomer(customerId);
    }

    public List<Lease> getLeasesByCar(Long carId) {
        log.info("[Admin] Fetching leases for car ID: {}", carId);
        return leaseService.getLeasesByCar(carId);
    }

    public void exportAsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=lease-history.csv");

        List<Lease> leases = leaseService.getAllLeases();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("LeaseID,CarModel,CustomerEmail,StartDate,EndDate");

            for (Lease lease : leases) {
                writer.printf("%d,%s,%s,%s,%s\n",
                        lease.getId(),
                        lease.getCar().getModel(),
                        lease.getCustomer().getEmail(),
                        lease.getStartDate(),
                        lease.getEndDate() != null ? lease.getEndDate() : "ONGOING"
                );
            }
        }
    }


    public void exportAsPdf(List<Lease> leases, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=lease-history.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Trimble Cars – Lease History", titleFont));
        document.add(new Paragraph("Generated: " + LocalDateTime.now()));
        document.add(Chunk.NEWLINE);

        for (Lease lease : leases) {
            String line = String.format("Lease ID: %d | Car: %s | Customer: %s | Start: %s | End: %s",
                    lease.getId(),
                    lease.getCar().getModel(),
                    lease.getCustomer().getEmail(),
                    lease.getStartDate(),
                    lease.getEndDate() != null ? lease.getEndDate() : "ONGOING");
            document.add(new Paragraph(line, bodyFont));
        }

        document.close();
    }

}
