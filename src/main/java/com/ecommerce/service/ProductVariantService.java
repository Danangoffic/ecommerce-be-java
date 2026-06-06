package com.ecommerce.service;

import com.ecommerce.dto.request.ProductVariantUpsertRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductVariant;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public ProductResponse addVariant(Long productId, ProductVariantUpsertRequest request) {
        Product product = productService.getManagedProduct(productId);
        if (productVariantRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ConflictException("Variant SKU already exists");
        }
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        apply(variant, request);
        productVariantRepository.save(variant);
        syncAggregateStock(product);
        return productService.toResponse(product);
    }

    @Transactional
    public ProductResponse updateVariant(Long productId, Long variantId, ProductVariantUpsertRequest request) {
        Product product = productService.getManagedProduct(productId);
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
        if (!variant.getSku().equalsIgnoreCase(request.sku())
                && productVariantRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ConflictException("Variant SKU already exists");
        }
        apply(variant, request);
        productVariantRepository.save(variant);
        syncAggregateStock(product);
        return productService.toResponse(product);
    }

    @Transactional
    public ProductResponse deleteVariant(Long productId, Long variantId) {
        Product product = productService.getManagedProduct(productId);
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
        productVariantRepository.delete(variant);
        productVariantRepository.flush();
        syncAggregateStock(product);
        return productService.toResponse(product);
    }

    private void apply(ProductVariant variant, ProductVariantUpsertRequest request) {
        variant.setSku(request.sku());
        variant.setSize(blankToNull(request.size()));
        variant.setColor(blankToNull(request.color()));
        variant.setPrice(request.price());
        variant.setStock(request.stock());
        variant.setStatus(parseStatus(request.status()));
    }

    /**
     * Keeps the parent product stock as the aggregate of its variant stock so that
     * existing reports, low-stock checks and the purchasable flag keep working.
     */
    private void syncAggregateStock(Product product) {
        productVariantRepository.flush();
        product.setStock(productVariantRepository.sumStockByProductId(product.getId()));
        productRepository.save(product);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ProductStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return ProductStatus.ACTIVE;
        }
        try {
            return ProductStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid variant status");
        }
    }
}
