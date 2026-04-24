package com.mezo.pos.product.infrastructure.adapter;

import com.mezo.pos.product.domain.port.ImageStorage;
import com.mezo.pos.shared.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
public class LocalImageStorage implements ImageStorage {

    private final String uploadDir;
    private final String baseUrl;

    public LocalImageStorage(
            @Value("${mezo.storage.upload-dir:./uploads}") String uploadDir,
            @Value("${mezo.storage.base-url:http://localhost:8080/uploads}") String baseUrl) {
        this.uploadDir = uploadDir;
        this.baseUrl = baseUrl;
    }

    @Override
    public String upload(byte[] file, String fileName, UUID businessId) {
        try {
            Path dir = Paths.get(uploadDir, businessId.toString());
            Files.createDirectories(dir);

            String uniqueName = UUID.randomUUID() + "_" + sanitize(fileName);
            Path filePath = dir.resolve(uniqueName);
            Files.write(filePath, file);

            log.info("Image saved to {}", filePath);
            return baseUrl + "/" + businessId + "/" + uniqueName;
        } catch (IOException e) {
            throw new DomainException("Failed to save image: " + e.getMessage());
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            String relativePath = imageUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
            log.info("Image deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }
    }

    private String sanitize(String fileName) {
        if (fileName == null) return "image";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
