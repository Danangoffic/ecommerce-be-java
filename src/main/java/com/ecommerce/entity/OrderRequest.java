package com.ecommerce.entity;

import com.ecommerce.entity.enums.OrderRequestStatus;
import com.ecommerce.entity.enums.OrderRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "order_requests")
public class OrderRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private OrderRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderRequestStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    @Column(name = "requested_amount", precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
