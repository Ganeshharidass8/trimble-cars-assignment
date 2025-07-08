package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.dto.UserRequestDTO;
import com.trimblecars.lease_service.dto.UserResponseDTO;
import com.trimblecars.lease_service.entity.Lease;
import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.model.ResponseModel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;


import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final CarService carService;
    private final LeaseService leaseService;

    // --- USER MANAGEMENT ---

    public ResponseEntity<ResponseModel<UserResponseDTO>> registerUser(UserRequestDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().body(ResponseModel.failure("Request body cannot be null", null));
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseModel.failure("Email cannot be null or empty", null));
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseModel.failure("Name cannot be null or empty", null));
        }

        if (dto.getRole() == null) {
            return ResponseEntity.badRequest().body(ResponseModel.failure("Role must be specified", null));
        }

        // Check if user already exists
        Optional<User> existing = userService.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            log.warn("User already exists with email: {}", dto.getEmail());

            User existingUser = existing.get();
            UserResponseDTO responseDTO = new UserResponseDTO();
            responseDTO.setId(existingUser.getId());
            responseDTO.setName(existingUser.getName());
            responseDTO.setEmail(existingUser.getEmail());
            responseDTO.setRole(existingUser.getRole().name());

            return ResponseEntity.ok(ResponseModel.failure("User already exists with this email.", responseDTO));
        }

        log.info("[Admin] Registering new user: {}", dto.getEmail());

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.valueOf(dto.getRole().name()));

        User saved = userService.registerUser(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole().name());

        return ResponseEntity.ok(ResponseModel.success("User registered successfully.", response));
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
