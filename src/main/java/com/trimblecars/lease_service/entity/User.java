package com.trimblecars.lease_service.entity;

import com.trimblecars.lease_service.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
