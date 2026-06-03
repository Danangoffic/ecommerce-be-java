package com.ecommerce.service;

import com.ecommerce.config.ApplicationProperties;
import com.ecommerce.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ApplicationProperties properties;

    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        try {
            Path directory = Path.of(properties.getStorage().getProductImagesDir()).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String originalName = file.getOriginalFilename() == null ? "product-image" : file.getOriginalFilename();
            String extension = extractExtension(originalName);
            String filename = UUID.randomUUID() + extension;
            Path target = directory.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/api/v1/files/products/" + filename;
        } catch (IOException exception) {
            throw new BadRequestException("Unable to store image");
        }
    }

    public Path resolveProductImage(String filename) {
        Path directory = Path.of(properties.getStorage().getProductImagesDir()).toAbsolutePath().normalize();
        Path resolved = directory.resolve(filename).normalize();
        if (!resolved.startsWith(directory)) {
            throw new BadRequestException("Invalid image path");
        }
        return resolved;
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot);
    }
}
