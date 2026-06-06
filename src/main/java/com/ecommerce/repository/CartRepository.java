package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    @Query("""
            select distinct c from Cart c
            left join fetch c.items i
            left join fetch i.product p
            left join fetch p.category
            left join fetch i.variant v
            where c.user.id = :userId and c.status = :status
            """)
    Optional<Cart> findDetailedByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CartStatus status);
}
