package com.ecommerce.controller;

import com.ecommerce.dto.request.WarehouseUpsertRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.WarehouseResponse;
import com.ecommerce.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/warehouses")
@RequiredArgsConstructor
public class AdminWarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<PageResponse<WarehouseResponse>> list(@RequestParam(required = false) Integer page,
                                                             @RequestParam(required = false) Integer size) {
        return ApiResponse.success("Warehouses fetched", warehouseService.list(page, size));
    }

    @GetMapping("/active")
    public ApiResponse<List<WarehouseResponse>> listActive() {
        return ApiResponse.success("Warehouses fetched", warehouseService.listActive());
    }

    @PostMapping
    public ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseUpsertRequest request) {
        return ApiResponse.success("Warehouse created", warehouseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseUpsertRequest request) {
        return ApiResponse.success("Warehouse updated", warehouseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<WarehouseResponse> deactivate(@PathVariable Long id) {
        return ApiResponse.success("Warehouse deactivated", warehouseService.deactivate(id));
    }
}
