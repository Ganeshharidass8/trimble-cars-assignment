package com.trimblecars.lease_service.repository;

import com.trimblecars.lease_service.entity.Lease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
    List<Lease> findByCustomerId(Long customerId);
    List<Lease> findByCarId(Long carId);
    long countByCustomerIdAndEndDateIsNull(Long customerId);
}
