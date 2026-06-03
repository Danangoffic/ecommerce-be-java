package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.enums.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p from Product p
            join fetch p.category c
            left join fetch p.warehouse w
            where (:activeOnly = false or p.status = com.ecommerce.entity.enums.ProductStatus.ACTIVE)
              and (:categoryId is null or c.id = :categoryId)
              and (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Product> search(@Param("activeOnly") boolean activeOnly,
                         @Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword,
                         Pageable pageable);

    @Query("""
            select p from Product p
            join fetch p.category
            left join fetch p.warehouse
            where p.id = :id
            """)
    Optional<Product> findDetailedById(@Param("id") Long id);

    @Query("""
            select p from Product p
            join fetch p.category
            left join fetch p.warehouse
            order by p.id
            """)
    List<Product> findAllDetailed();

    long countByStatus(ProductStatus status);

    @Query("""
            select p from Product p
            join fetch p.category
            left join fetch p.warehouse
            where p.status = com.ecommerce.entity.enums.ProductStatus.ACTIVE
              and p.stock <= p.minimumStockLevel
            order by p.stock asc, p.id asc
            """)
    List<Product> findLowStockProducts();

    boolean existsByCategoryId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
