package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductUpsertRequest;
import com.ecommerce.dto.request.UpdateProductStatusRequest;
import com.ecommerce.dto.request.UpdateProductStockRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.ProductImportResultResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ApiResponse.success("Products fetched", productService.listAdmin(categoryId, keyword, page, size, sort));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductUpsertRequest request) {
        return ApiResponse.success("Product created", productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpsertRequest request) {
        return ApiResponse.success("Product updated", productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<ProductResponse> deactivate(@PathVariable Long id) {
        return ApiResponse.success("Product deactivated", productService.deactivate(id));
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<ProductResponse> updateStock(@PathVariable Long id, @Valid @RequestBody UpdateProductStockRequest request) {
        return ApiResponse.success("Product stock updated", productService.updateStock(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ProductResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateProductStatusRequest request) {
        return ApiResponse.success("Product status updated", productService.updateStatus(id, request));
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> updateImage(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Product image updated", productService.updateImage(id, file));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductImportResultResponse> importProducts(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Products imported", productService.importProducts(file));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String format) {
        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("xlsx".equalsIgnoreCase(format)) {
            bytes = productService.exportProductsExcel();
            filename = "products.xlsx";
            contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            bytes = productService.exportProductsCsv();
            filename = "products.csv";
            contentType = MediaType.parseMediaType("text/csv");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(bytes);
    }
}
