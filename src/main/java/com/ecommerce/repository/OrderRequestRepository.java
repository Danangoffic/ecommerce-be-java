package com.ecommerce.repository;

import com.ecommerce.entity.OrderRequest;
import com.ecommerce.entity.enums.OrderRequestStatus;
import com.ecommerce.entity.enums.OrderRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    List<OrderRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OrderRequest> findByStatusOrderByCreatedAtDesc(OrderRequestStatus status);

    List<OrderRequest> findByTypeOrderByCreatedAtDesc(OrderRequestType type);

    List<OrderRequest> findByTypeAndStatusOrderByCreatedAtDesc(OrderRequestType type, OrderRequestStatus status);

    Optional<OrderRequest> findByIdAndUserId(Long id, Long userId);
}
