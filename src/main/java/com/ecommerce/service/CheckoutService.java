package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.CheckoutResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.enums.OrderStatus;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartService.getActiveCartForCheckout(userId);
        List<Long> productIds = cart.getItems().stream().map(item -> item.getProduct().getId()).toList();
        List<Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds);
        Map<Long, Product> productMap = new HashMap<>();
        lockedProducts.forEach(product -> productMap.put(product.getId(), product));

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
            if (product == null || product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Stock is not sufficient for checkout");
            }

            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(price);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        cartService.markCheckedOut(cart);
        cartService.createFreshActiveCart(userId);
        return new CheckoutResponse(savedOrder.getId(), savedOrder.getOrderNumber());
    }
}
