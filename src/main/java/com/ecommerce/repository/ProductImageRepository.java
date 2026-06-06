package com.ecommerce.repository;

import com.ecommerce.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAscIdAsc(Long productId);

    List<ProductImage> findByProductIdInOrderBySortOrderAscIdAsc(List<Long> productIds);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    @Modifying
    @Query("update ProductImage pi set pi.primary = false where pi.product.id = :productId")
    void clearPrimaryForProduct(@Param("productId") Long productId);
}
