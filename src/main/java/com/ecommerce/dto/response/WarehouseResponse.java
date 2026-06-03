package com.ecommerce.dto.response;

public record WarehouseResponse(
        Long id,
        String code,
        String name,
        String location,
        String status
) {
}
