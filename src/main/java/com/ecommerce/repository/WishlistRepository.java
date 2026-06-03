package com.ecommerce.repository;

import com.ecommerce.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("""
            select w from Wishlist w
            join fetch w.product p
            join fetch p.category
            left join fetch p.warehouse
            where w.user.id = :userId
            """)
    Page<Wishlist> findDetailedByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
