package com.trimblecars.lease_service.dto;

import com.trimblecars.lease_service.enums.UserRole;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String email;
    private UserRole role;
}
