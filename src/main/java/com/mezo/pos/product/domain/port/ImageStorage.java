package com.mezo.pos.product.domain.port;

import java.util.UUID;

public interface ImageStorage {

    String upload(byte[] file, String fileName, UUID businessId);

    void delete(String imageUrl);
}
