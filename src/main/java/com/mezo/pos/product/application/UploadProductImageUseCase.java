package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.port.ImageStorage;
import com.mezo.pos.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadProductImageUseCase {

    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3MB

    private final ImageStorage imageStorage;

    public String execute(byte[] file, String fileName, UUID businessId) {
        if (file == null || file.length == 0) {
            throw new DomainException("File cannot be empty");
        }

        if (file.length > MAX_FILE_SIZE) {
            throw new DomainException("File size exceeds maximum allowed size of 3MB");
        }

        return imageStorage.upload(file, fileName, businessId);
    }
}
