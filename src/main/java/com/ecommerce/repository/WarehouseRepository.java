package com.ecommerce.repository;

import com.ecommerce.entity.Warehouse;
import com.ecommerce.entity.enums.WarehouseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByCodeIgnoreCase(String code);

    Optional<Warehouse> findFirstByStatusOrderByCreatedAtAsc(WarehouseStatus status);

    List<Warehouse> findAllByOrderByNameAsc();
}
