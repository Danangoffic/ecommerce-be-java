package com.ecommerce.entity;

import com.ecommerce.entity.enums.ProductStatus;
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

@Getter
@Setter
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String color;

    /**
     * Optional variant-specific price. When null, the parent product price applies.
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Human-readable variant label combining size and color, e.g. "L / Red".
     * Returns null when neither attribute is set.
     */
    public String label() {
        boolean hasSize = size != null && !size.isBlank();
        boolean hasColor = color != null && !color.isBlank();
        if (hasSize && hasColor) {
            return size + " / " + color;
        }
        if (hasSize) {
            return size;
        }
        if (hasColor) {
            return color;
        }
        return null;
    }

    /**
     * Variant price when set, otherwise falls back to the parent product price.
     */
    public BigDecimal effectivePrice() {
        return price != null ? price : product.getPrice();
    }
}
