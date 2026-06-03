package com.ecommerce.dto.response;

import java.util.List;

public record ProductImportResultResponse(
        int processed,
        int created,
        int updated,
        int skipped,
        List<String> errors
) {
}
