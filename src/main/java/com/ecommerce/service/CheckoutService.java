package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.CheckoutResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductVariant;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;
import com.ecommerce.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartService.getActiveCartForCheckout(userId);
        List<Long> productIds = cart.getItems().stream().map(item -> item.getProduct().getId()).toList();
        List<Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds);
        Map<Long, Product> productMap = new HashMap<>();
        lockedProducts.forEach(product -> productMap.put(product.getId(), product));

        List<Long> variantIds = cart.getItems().stream()
                .map(item -> item.getVariant() == null ? null : item.getVariant().getId())
                .filter(Objects::nonNull)
                .toList();
        Map<Long, ProductVariant> variantMap = new HashMap<>();
        if (!variantIds.isEmpty()) {
            productVariantRepository.findAllByIdForUpdate(variantIds)
                    .forEach(variant -> variantMap.put(variant.getId(), variant));
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setShippingAddress(request.shippingAddress());
        order.setNotes(request.notes());
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        for (var cartItem : cart.getItems()) {
            Product product = productMap.get(cartItem.getProduct().getId());
            if (product == null) {
                throw new InsufficientStockException("Stock is not sufficient for checkout");
            }
            ProductVariant variant = cartItem.getVariant() == null
                    ? null
                    : variantMap.get(cartItem.getVariant().getId());

            int quantity = cartItem.getQuantity();
            BigDecimal price;
            if (cartItem.getVariant() != null) {
                if (variant == null || variant.getStock() < quantity) {
                    throw new InsufficientStockException("Stock is not sufficient for checkout");
                }
                price = variant.getPrice() != null ? variant.getPrice() : product.getPrice();
            } else {
                if (product.getStock() < quantity) {
                    throw new InsufficientStockException("Stock is not sufficient for checkout");
                }
                price = product.getPrice();
            }

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            if (variant != null) {
                orderItem.setVariantId(variant.getId());
                orderItem.setVariantSku(variant.getSku());
                orderItem.setVariantLabel(variant.label());
            }
            orderItem.setPrice(price);
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);

            if (variant != null) {
                variant.setStock(variant.getStock() - quantity);
                // product stock is the aggregate of variant stock; keep it consistent
                product.setStock(Math.max(0, product.getStock() - quantity));
            } else {
                product.setStock(product.getStock() - quantity);
            }
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        cartService.markCheckedOut(cart);
        cartService.createFreshActiveCart(userId);
        return new CheckoutResponse(savedOrder.getId(), savedOrder.getOrderNumber());
    }
}
