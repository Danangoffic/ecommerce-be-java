package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/images")
@RequiredArgsConstructor
public class AdminProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> addImage(@PathVariable Long productId,
                                                 @RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "primary", defaultValue = "false") boolean primary) {
        return ApiResponse.success("Product image added", productImageService.addImage(productId, file, primary));
    }

    @PatchMapping("/{imageId}/primary")
    public ApiResponse<ProductResponse> setPrimary(@PathVariable Long productId, @PathVariable Long imageId) {
        return ApiResponse.success("Primary image updated", productImageService.setPrimary(productId, imageId));
    }

    @DeleteMapping("/{imageId}")
    public ApiResponse<ProductResponse> deleteImage(@PathVariable Long productId, @PathVariable Long imageId) {
        return ApiResponse.success("Product image deleted", productImageService.deleteImage(productId, imageId));
    }
}
