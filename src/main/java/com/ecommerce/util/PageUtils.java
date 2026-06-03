package com.ecommerce.util;

import com.ecommerce.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageUtils {

    private final ApplicationProperties properties;

    public Pageable pageable(Integer page, Integer size, Sort sort) {
        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size < 1
                ? properties.getPagination().getDefaultPageSize()
                : Math.min(size, properties.getPagination().getMaxPageSize());
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }
}
