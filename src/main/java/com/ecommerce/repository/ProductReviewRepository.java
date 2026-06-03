package com.ecommerce.repository;

import com.ecommerce.dto.response.ReviewStats;
import com.ecommerce.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    @Query("""
            select r from ProductReview r
            join fetch r.user
            where r.product.id = :productId
            """)
    Page<ProductReview> findDetailedByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("""
            select r from ProductReview r
            join fetch r.product p
            join fetch p.category
            left join fetch p.warehouse
            where r.user.id = :userId
            """)
    Page<ProductReview> findDetailedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select new com.ecommerce.dto.response.ReviewStats(coalesce(avg(r.rating), 0.0), count(r)) from ProductReview r where r.product.id = :productId")
    ReviewStats getStatsForProduct(@Param("productId") Long productId);

    @Query("""
            select r.product.id, coalesce(avg(r.rating), 0.0), count(r)
            from ProductReview r
            where r.product.id in :productIds
            group by r.product.id
            """)
    List<Object[]> findStatsForProductIds(@Param("productIds") List<Long> productIds);

    @Query("""
            select r from ProductReview r
            join fetch r.product p
            join fetch r.user u
            where (:rating is null or r.rating = :rating)
              and (:keyword is null or lower(r.comment) like lower(concat('%', :keyword, '%')) or lower(p.name) like lower(concat('%', :keyword, '%')) or lower(u.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<ProductReview> searchAdmin(@Param("rating") Integer rating,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
