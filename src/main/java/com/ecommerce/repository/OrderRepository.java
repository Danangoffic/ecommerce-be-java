package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            where o.user.id = :userId and o.id = :orderId
            """)
    Optional<Order> findDetailedByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            where o.id = :orderId
            """)
    Optional<Order> findDetailedById(@Param("orderId") Long orderId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("""
            select o from Order o
            where (:status is null or o.status = :status)
              and (:orderNumber is null or lower(o.orderNumber) like lower(concat('%', :orderNumber, '%')))
            """)
    Page<Order> searchAdmin(@Param("status") OrderStatus status,
                            @Param("orderNumber") String orderNumber,
                            Pageable pageable);

    List<Order> findByCreatedAtBetweenOrderByCreatedAtAsc(java.time.Instant from, java.time.Instant to);

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            where o.createdAt between :from and :to
            order by o.createdAt asc
            """)
    List<Order> findDetailedByCreatedAtBetween(@Param("from") java.time.Instant from, @Param("to") java.time.Instant to);

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from Order o
            where o.status <> com.ecommerce.entity.enums.OrderStatus.CANCELLED
            """)
    BigDecimal sumValidSales();

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query("""
            select count(o) > 0
            from Order o
            join o.items oi
            where o.user.id = :userId
              and oi.productId = :productId
              and o.status = com.ecommerce.entity.enums.OrderStatus.COMPLETED
            """)
    boolean hasCompletedOrderWithProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
