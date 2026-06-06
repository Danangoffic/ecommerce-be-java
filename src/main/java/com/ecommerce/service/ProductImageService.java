package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductImageRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse addImage(Long productId, MultipartFile file, boolean primary) {
        Product product = productService.getManagedProduct(productId);
        String url = fileStorageService.storeProductImage(file);

        List<ProductImage> existing = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(productId);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(url);
        image.setSortOrder(existing.size());

        boolean makePrimary = primary || existing.isEmpty();
        if (makePrimary) {
            existing.forEach(existingImage -> existingImage.setPrimary(false));
            productImageRepository.saveAll(existing);
            image.setPrimary(true);
            product.setImageUrl(url);
            productRepository.save(product);
        }
        productImageRepository.save(image);
        return productService.toResponse(product);
    }

    @Transactional
    public ProductResponse setPrimary(Long productId, Long imageId) {
        Product product = productService.getManagedProduct(productId);
        List<ProductImage> all = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(productId);
        ProductImage target = all.stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        all.forEach(image -> image.setPrimary(image.getId().equals(imageId)));
        productImageRepository.saveAll(all);

        product.setImageUrl(target.getImageUrl());
        productRepository.save(product);
        return productService.toResponse(product);
    }

    @Transactional
    public ProductResponse deleteImage(Long productId, Long imageId) {
        Product product = productService.getManagedProduct(productId);
        List<ProductImage> all = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(productId);
        ProductImage target = all.stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        boolean wasPrimary = target.isPrimary();
        all.remove(target);
        productImageRepository.delete(target);

        if (wasPrimary) {
            if (!all.isEmpty()) {
                ProductImage newPrimary = all.get(0);
                newPrimary.setPrimary(true);
                productImageRepository.save(newPrimary);
                product.setImageUrl(newPrimary.getImageUrl());
            } else {
                product.setImageUrl(null);
            }
            productRepository.save(product);
        }
        return productService.toResponse(product);
    }
}
