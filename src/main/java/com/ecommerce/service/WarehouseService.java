package com.ecommerce.service;

import com.ecommerce.dto.request.WarehouseUpsertRequest;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.WarehouseResponse;
import com.ecommerce.entity.Warehouse;
import com.ecommerce.entity.enums.WarehouseStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.WarehouseRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final PageUtils pageUtils;

    public PageResponse<WarehouseResponse> list(Integer page, Integer size) {
        Page<WarehouseResponse> result = warehouseRepository.findAll(pageUtils.pageable(page, size, Sort.by("name").ascending()))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    public List<WarehouseResponse> listActive() {
        return warehouseRepository.findAllByOrderByNameAsc().stream()
                .filter(warehouse -> warehouse.getStatus() == WarehouseStatus.ACTIVE)
                .map(this::toResponse)
                .toList();
    }

    public WarehouseResponse get(Long id) {
        return toResponse(getWarehouse(id));
    }

    public Warehouse getActiveWarehouse(Long id) {
        Warehouse warehouse = getWarehouse(id);
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new BadRequestException("Warehouse is inactive");
        }
        return warehouse;
    }

    public Warehouse getActiveWarehouseByCode(String code) {
        Warehouse warehouse = warehouseRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
            throw new BadRequestException("Warehouse is inactive");
        }
        return warehouse;
    }

    public Warehouse getDefaultWarehouse() {
        return warehouseRepository.findFirstByStatusOrderByCreatedAtAsc(WarehouseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active warehouse found"));
    }

    @Transactional
    public WarehouseResponse create(WarehouseUpsertRequest request) {
        if (warehouseRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new BadRequestException("Warehouse code already exists");
        }
        Warehouse warehouse = new Warehouse();
        apply(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse update(Long id, WarehouseUpsertRequest request) {
        Warehouse warehouse = getWarehouse(id);
        warehouseRepository.findByCodeIgnoreCase(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Warehouse code already exists");
                });
        apply(warehouse, request);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse deactivate(Long id) {
        Warehouse warehouse = getWarehouse(id);
        warehouse.setStatus(WarehouseStatus.INACTIVE);
        return toResponse(warehouseRepository.save(warehouse));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
    }

    private void apply(Warehouse warehouse, WarehouseUpsertRequest request) {
        warehouse.setCode(request.code().trim().toUpperCase());
        warehouse.setName(request.name().trim());
        warehouse.setLocation(request.location());
        warehouse.setStatus(parseStatus(request.status()));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getStatus().name()
        );
    }

    private WarehouseStatus parseStatus(String value) {
        try {
            return WarehouseStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid warehouse status");
        }
    }
}
