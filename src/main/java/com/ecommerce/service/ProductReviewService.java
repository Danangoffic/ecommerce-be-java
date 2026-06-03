package com.ecommerce.service;

import com.ecommerce.dto.request.ProductReviewRequest;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.response.ProductReviewResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductReview;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PageUtils pageUtils;

    @Transactional
    public ProductReviewResponse addReview(Long userId, ProductReviewRequest request) {
        if (productReviewRepository.existsByUserIdAndProductId(userId, request.productId())) {
            throw new ConflictException("You have already reviewed this product");
        }

        // Verified purchase check
        boolean purchased = orderRepository.hasCompletedOrderWithProduct(userId, request.productId());
        if (!purchased) {
            throw new BadRequestException("You can only review products you have purchased and completed the order");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findDetailedById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.rating());
        review.setComment(request.comment());
        ProductReview saved = productReviewRepository.save(review);

        return toResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductReviewResponse> getProductReviews(Long productId, Integer page, Integer size) {
        Page<ProductReviewResponse> result = productReviewRepository.findDetailedByProductId(
                productId, pageUtils.pageable(page, size, null))
                .map(review -> toResponse(review, false)); // Don't include product details to save bandwidth
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductReviewResponse> getUserReviews(Long userId, Integer page, Integer size) {
        Page<ProductReviewResponse> result = productReviewRepository.findDetailedByUserId(
                userId, pageUtils.pageable(page, size, null))
                .map(review -> toResponse(review, true)); // Include product details
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductReviewResponse> searchAdminReviews(Integer rating, String keyword, Integer page, Integer size) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<ProductReviewResponse> result = productReviewRepository.searchAdmin(
                rating, cleanKeyword, pageUtils.pageable(page, size, null))
                .map(review -> toResponse(review, true)); // Include product details
        return PageResponse.from(result);
    }

    @Transactional
    public void deleteReview(Long id) {
        ProductReview review = productReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        productReviewRepository.delete(review);
    }

    private ProductReviewResponse toResponse(ProductReview review, boolean includeProduct) {
        ProductResponse productResponse = null;
        if (includeProduct && review.getProduct() != null) {
            productResponse = productService.toResponse(review.getProduct());
        }
        return new ProductReviewResponse(
                review.getId(),
                productResponse,
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
