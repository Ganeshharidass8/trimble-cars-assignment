package com.trimblecars.lease_service.repository;

import com.trimblecars.lease_service.entity.Car;
import com.trimblecars.lease_service.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByStatus(CarStatus status);
    List<Car> findByOwnerId(Long ownerId);
}
