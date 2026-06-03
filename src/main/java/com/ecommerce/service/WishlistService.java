package com.ecommerce.service;

import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.response.WishlistResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.Wishlist;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PageUtils pageUtils;

    @Transactional
    public WishlistResponse add(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ConflictException("Product is already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findDetailedById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        Wishlist saved = wishlistRepository.save(wishlist);

        return toResponse(saved);
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist entry not found"));
        wishlistRepository.delete(wishlist);
    }

    @Transactional(readOnly = true)
    public PageResponse<WishlistResponse> list(Long userId, Integer page, Integer size) {
        Page<WishlistResponse> result = wishlistRepository.findDetailedByUserId(
                userId, pageUtils.pageable(page, size, null))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public boolean check(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    private WishlistResponse toResponse(Wishlist wishlist) {
        ProductResponse productResponse = productService.toResponse(wishlist.getProduct());
        return new WishlistResponse(
                wishlist.getId(),
                productResponse,
                wishlist.getCreatedAt()
        );
    }
}
