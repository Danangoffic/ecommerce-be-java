package com.ecommerce.repository;

import com.ecommerce.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

    List<ProductVariant> findByProductIdInOrderByIdAsc(List<Long> productIds);

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    long countByProductId(Long productId);

    boolean existsBySkuIgnoreCase(String sku);

    @Query("select coalesce(sum(v.stock), 0) from ProductVariant v where v.product.id = :productId")
    int sumStockByProductId(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v where v.id in :ids")
    List<ProductVariant> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
