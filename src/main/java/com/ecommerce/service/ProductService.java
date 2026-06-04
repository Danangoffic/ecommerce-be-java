package com.ecommerce.service;

import com.ecommerce.dto.request.ProductUpsertRequest;
import com.ecommerce.dto.request.UpdateProductStatusRequest;
import com.ecommerce.dto.request.UpdateProductStockRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.dto.response.ProductImportResultResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.dto.response.WarehouseResponse;
import com.ecommerce.dto.response.ReviewStats;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Warehouse;
import com.ecommerce.entity.enums.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductReviewRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final WarehouseService warehouseService;
    private final FileStorageService fileStorageService;
    private final PageUtils pageUtils;
    private final ProductReviewRepository productReviewRepository;
    private final WishlistRepository wishlistRepository;

    public PageResponse<ProductResponse> listPublic(Long categoryId, String keyword, Integer page, Integer size, String sort) {
        return listPublic(categoryId, keyword, page, size, sort, null);
    }

    public PageResponse<ProductResponse> listPublic(Long categoryId, String keyword, Integer page, Integer size, String sort, Long userId) {
        Page<Product> result = productRepository.search(
                        true,
                        categoryId,
                        blankToNull(keyword),
                        pageUtils.pageable(page, size, resolveSort(sort)));
        return mapPageWithStats(result, userId);
    }

    public PageResponse<ProductResponse> listAdmin(Long categoryId, String keyword, Integer page, Integer size, String sort) {
        Page<Product> result = productRepository.search(
                        false,
                        categoryId,
                        blankToNull(keyword),
                        pageUtils.pageable(page, size, resolveSort(sort)));
        return mapPageWithStats(result, null);
    }

    public ProductResponse getPublicDetail(Long id) {
        return getPublicDetail(id, null);
    }

    public ProductResponse getPublicDetail(Long id, Long userId) {
        Product product = getProduct(id);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found");
        }
        return toResponse(product, userId);
    }

    public Product getManagedProduct(Long id) {
        return getProduct(id);
    }

    @Transactional
    public ProductResponse create(ProductUpsertRequest request) {
        Product product = new Product();
        apply(product, request, false);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpsertRequest request) {
        Product product = getProduct(id);
        apply(product, request, true);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateStock(Long id, UpdateProductStockRequest request) {
        Product product = getProduct(id);
        product.setStock(request.stock());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateStatus(Long id, UpdateProductStatusRequest request) {
        Product product = getProduct(id);
        product.setStatus(parseStatus(request.status()));
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse deactivate(Long id) {
        Product product = getProduct(id);
        product.setStatus(ProductStatus.INACTIVE);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public byte[] exportProductsCsv() {
        StringBuilder builder = new StringBuilder();
        builder.append("id,categoryId,warehouseId,warehouseCode,name,description,price,stock,minimumStockLevel,imageUrl,status\n");
        for (Product product : productRepository.findAllDetailed()) {
            builder.append(value(product.getId())).append(',')
                    .append(value(product.getCategory().getId())).append(',')
                    .append(value(product.getWarehouse() == null ? null : product.getWarehouse().getId())).append(',')
                    .append(escape(product.getWarehouse() == null ? null : product.getWarehouse().getCode())).append(',')
                    .append(escape(product.getName())).append(',')
                    .append(escape(product.getDescription())).append(',')
                    .append(value(product.getPrice())).append(',')
                    .append(value(product.getStock())).append(',')
                    .append(value(product.getMinimumStockLevel())).append(',')
                    .append(escape(product.getImageUrl())).append(',')
                    .append(product.getStatus().name())
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportProductsExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");
            String[] headers = {"id", "categoryId", "warehouseId", "warehouseCode", "name", "description", "price", "stock", "minimumStockLevel", "imageUrl", "status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (Product product : productRepository.findAllDetailed()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getCategory().getId());
                if (product.getWarehouse() != null) {
                    row.createCell(2).setCellValue(product.getWarehouse().getId());
                    row.createCell(3).setCellValue(product.getWarehouse().getCode());
                }
                row.createCell(4).setCellValue(product.getName());
                row.createCell(5).setCellValue(product.getDescription() == null ? "" : product.getDescription());
                row.createCell(6).setCellValue(product.getPrice().doubleValue());
                row.createCell(7).setCellValue(product.getStock());
                row.createCell(8).setCellValue(product.getMinimumStockLevel() == null ? 0 : product.getMinimumStockLevel());
                row.createCell(9).setCellValue(product.getImageUrl() == null ? "" : product.getImageUrl());
                row.createCell(10).setCellValue(product.getStatus().name());
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export products", exception);
        }
    }

    @Transactional
    public ProductImportResultResponse importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Import file is required");
        }
        try {
            List<Map<String, String>> rows = isExcel(file.getOriginalFilename())
                    ? readExcel(file)
                    : readCsv(file);
            int processed = 0;
            int created = 0;
            int updated = 0;
            int skipped = 0;
            List<String> errors = new ArrayList<>();
            for (Map<String, String> row : rows) {
                processed++;
                try {
                    ImportOutcome outcome = upsertFromImportRow(row);
                    created += outcome.created ? 1 : 0;
                    updated += outcome.updated ? 1 : 0;
                    skipped += outcome.skipped ? 1 : 0;
                } catch (Exception exception) {
                    errors.add("Row " + processed + ": " + exception.getMessage());
                }
            }
            return new ProductImportResultResponse(processed, created, updated, skipped, errors);
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read import file");
        }
    }

    @Transactional
    public ProductResponse updateImage(Long id, MultipartFile file) {
        Product product = getProduct(id);
        product.setImageUrl(fileStorageService.storeProductImage(file));
        return toResponse(productRepository.save(product));
    }

    private void apply(Product product, ProductUpsertRequest request, boolean updating) {
        Category category = categoryService.getActiveCategory(request.categoryId());
        product.setCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setStatus(parseStatus(request.status()));
        if (request.minimumStockLevel() != null) {
            product.setMinimumStockLevel(request.minimumStockLevel());
        } else if (!updating && product.getMinimumStockLevel() == null) {
            product.setMinimumStockLevel(0);
        }
        if (request.warehouseId() != null) {
            product.setWarehouse(warehouseService.getActiveWarehouse(request.warehouseId()));
        } else if (!updating && product.getWarehouse() == null) {
            product.setWarehouse(warehouseService.getDefaultWarehouse());
        }
    }

    private Product getProduct(Long id) {
        return productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductResponse toResponse(Product product) {
        return toResponse(product, null);
    }

    public ProductResponse toResponse(Product product, Long userId) {
        ReviewStats stats = productReviewRepository.getStatsForProduct(product.getId());
        boolean isInWishlist = userId != null && wishlistRepository.existsByUserIdAndProductId(userId, product.getId());
        return toResponse(product, stats.averageRating(), stats.reviewCount(), isInWishlist);
    }

    public ProductResponse toResponse(Product product, Double averageRating, Long reviewCount) {
        return toResponse(product, averageRating, reviewCount, false);
    }

    public ProductResponse toResponse(Product product, Double averageRating, Long reviewCount, boolean isInWishlist) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getMinimumStockLevel(),
                product.getStatus() == ProductStatus.ACTIVE && product.getStock() > 0,
                product.getMinimumStockLevel() != null && product.getStock() <= product.getMinimumStockLevel(),
                product.getImageUrl(),
                product.getStatus().name(),
                new CategoryResponse(
                        product.getCategory().getId(),
                        product.getCategory().getName(),
                        product.getCategory().getDescription(),
                        product.getCategory().getStatus().name()
                ),
                toWarehouseResponse(product.getWarehouse()),
                averageRating,
                reviewCount,
                isInWishlist,
                product.getCreatedAt()
        );
    }

    private PageResponse<ProductResponse> mapPageWithStats(Page<Product> productPage) {
        return mapPageWithStats(productPage, null);
    }

    private PageResponse<ProductResponse> mapPageWithStats(Page<Product> productPage, Long userId) {
        List<Long> productIds = productPage.getContent().stream().map(Product::getId).toList();
        Map<Long, ReviewStats> statsMap = new HashMap<>();
        Map<Long, Boolean> wishlistMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Object[]> statsList = productReviewRepository.findStatsForProductIds(productIds);
            for (Object[] row : statsList) {
                Long prodId = (Long) row[0];
                Double avgRating = (Double) row[1];
                Long revCount = (Long) row[2];
                statsMap.put(prodId, new ReviewStats(avgRating, revCount));
            }
            if (userId != null) {
                List<Long> wishlistedIds = wishlistRepository.findProductIdsByUserIdAndProductIds(userId, productIds);
                for (Long prodId : productIds) {
                    wishlistMap.put(prodId, wishlistedIds.contains(prodId));
                }
            }
        }
        Page<ProductResponse> responsePage = productPage.map(product -> {
            ReviewStats stats = statsMap.getOrDefault(product.getId(), new ReviewStats(0.0, 0L));
            boolean isInWishlist = wishlistMap.getOrDefault(product.getId(), false);
            return toResponse(product, stats.averageRating(), stats.reviewCount(), isInWishlist);
        });
        return PageResponse.from(responsePage);
    }

    private Sort resolveSort(String sort) {
        if ("price".equalsIgnoreCase(sort)) {
            return Sort.by("price").ascending();
        }
        return Sort.by("createdAt").descending();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ProductStatus parseStatus(String value) {
        try {
            return ProductStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid product status");
        }
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isExcel(String originalFilename) {
        return originalFilename != null && originalFilename.toLowerCase(Locale.ROOT).endsWith(".xlsx");
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return List.of();
            }
            List<String> headers = parseCsvLine(headerLine);
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                rows.add(mapRow(headers, values));
            }
            return rows;
        }
    }

    private List<Map<String, String>> readExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return List.of();
            }
            List<String> headers = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();
            for (Cell cell : headerRow) {
                headers.add(formatter.formatCellValue(cell).trim());
            }
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                List<String> values = new ArrayList<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.add(cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                rows.add(mapRow(headers, values));
            }
            return rows;
        }
    }

    private Map<String, String> mapRow(List<String> headers, List<String> values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i), i < values.size() ? values.get(i) : "");
        }
        return row;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (ch == '"') {
                    quoted = false;
                } else {
                    current.append(ch);
                }
            } else if (ch == ',') {
                values.add(current.toString().trim());
                current.setLength(0);
            } else if (ch == '"') {
                quoted = true;
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private ImportOutcome upsertFromImportRow(Map<String, String> row) {
        String idValue = blankToNull(row.get("id"));
        Product product = null;
        boolean created = false;
        if (idValue != null) {
            product = productRepository.findDetailedById(Long.valueOf(idValue))
                    .orElse(null);
        }
        if (product == null) {
            product = new Product();
            created = true;
        }

        String categoryIdValue = blankToNull(row.get("categoryId"));
        if (categoryIdValue == null) {
            throw new BadRequestException("categoryId is required");
        }
        product.setCategory(categoryService.getActiveCategory(Long.valueOf(categoryIdValue)));
        product.setName(require(row, "name"));
        product.setDescription(blankToNull(row.get("description")));
        product.setPrice(new BigDecimal(require(row, "price")));
        product.setStock(Integer.valueOf(require(row, "stock")));
        product.setMinimumStockLevel(parseIntegerOrDefault(row.get("minimumStockLevel"), 0));
        product.setImageUrl(blankToNull(row.get("imageUrl")));
        product.setStatus(parseStatus(require(row, "status")));
        String warehouseIdValue = blankToNull(row.get("warehouseId"));
        String warehouseCodeValue = blankToNull(row.get("warehouseCode"));
        if (warehouseIdValue != null) {
            product.setWarehouse(warehouseService.getActiveWarehouse(Long.valueOf(warehouseIdValue)));
        } else if (warehouseCodeValue != null) {
            product.setWarehouse(warehouseService.getActiveWarehouseByCode(warehouseCodeValue));
        } else if (created) {
            product.setWarehouse(warehouseService.getDefaultWarehouse());
        }

        productRepository.save(product);
        return new ImportOutcome(created, !created, false);
    }

    private String require(Map<String, String> row, String key) {
        String value = blankToNull(row.get(key));
        if (value == null) {
            throw new BadRequestException(key + " is required");
        }
        return value;
    }

    private Integer parseIntegerOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.valueOf(value);
    }

    private record ImportOutcome(boolean created, boolean updated, boolean skipped) {
    }

    private WarehouseResponse toWarehouseResponse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getStatus().name()
        );
    }
}
