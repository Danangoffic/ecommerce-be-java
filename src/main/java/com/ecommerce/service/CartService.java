package com.ecommerce.service;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.enums.CartStatus;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateDetailedCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateDetailedCart(userId);
        Product product = productService.getManagedProduct(request.productId());
        validateProductPurchasable(product, request.quantity());

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity = request.quantity();
        if (existingItem != null) {
            newQuantity += existingItem.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException("Quantity exceeds available stock");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setPriceSnapshot(product.getPrice());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.quantity());
            item.setPriceSnapshot(product.getPrice());
            cart.getItems().add(item);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        validateProductPurchasable(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        item.setPriceSnapshot(item.getProduct().getPrice());
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateDetailedCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clear(Long userId) {
        Cart cart = getOrCreateDetailedCart(userId);
        cart.setItems(new ArrayList<>());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public Cart getActiveCartForCheckout(Long userId) {
        Cart cart = getOrCreateDetailedCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        return cart;
    }

    @Transactional
    public void markCheckedOut(Cart cart) {
        cart.getItems().clear();
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);
    }

    @Transactional
    public Cart createFreshActiveCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        return cartRepository.save(cart);
    }

    private Cart getOrCreateDetailedCart(Long userId) {
        return cartRepository.findDetailedByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = createFreshActiveCart(userId);
                    return cartRepository.findDetailedByUserIdAndStatus(userId, CartStatus.ACTIVE).orElse(cart);
                });
    }

    private void validateProductPurchasable(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is inactive");
        }
        if (quantity > product.getStock()) {
            throw new InsufficientStockException("Quantity exceeds available stock");
        }
    }

    private CartResponse toResponse(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(
                cart.getId(),
                cart.getStatus().name(),
                cart.getItems().stream()
                        .map(item -> new CartItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPriceSnapshot(),
                                item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))
                        ))
                        .toList(),
                subtotal
        );
    }
}
