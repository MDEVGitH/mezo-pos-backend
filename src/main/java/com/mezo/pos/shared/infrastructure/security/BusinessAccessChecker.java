package com.mezo.pos.shared.infrastructure.security;

import java.util.UUID;

public interface BusinessAccessChecker {
    boolean belongsToBusiness(UUID userId, UUID businessId);
}
