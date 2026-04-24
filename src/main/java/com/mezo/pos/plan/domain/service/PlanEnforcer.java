package com.mezo.pos.plan.domain.service;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.port.PlanRepository;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.exception.PlanExpiredException;
import com.mezo.pos.shared.domain.exception.PlanLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlanEnforcer {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public void validatePlanNotExpired(User owner) {
        if (owner.getPlanExpiresAt() != null
                && LocalDateTime.now().isAfter(owner.getPlanExpiresAt())) {
            throw new PlanExpiredException(
                    "Tu plan " + owner.getPlan() + " ha expirado. "
                            + "Renueva tu suscripcion para continuar."
            );
        }
    }

    public void validateCanCreateBusiness(User owner, long currentBusinessCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (plan.getMaxBusinesses() != -1 && currentBusinessCount >= plan.getMaxBusinesses()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " permite maximo " + plan.getMaxBusinesses() + " negocio(s)"
            );
        }
    }

    public void validateCanCreateTable(User owner, long currentTableCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (plan.getMaxTables() != -1 && currentTableCount >= plan.getMaxTables()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " permite maximo " + plan.getMaxTables() + " mesas"
            );
        }
    }

    public void validateCanCreateProduct(User owner, long currentProductCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (plan.getMaxProducts() != -1 && currentProductCount >= plan.getMaxProducts()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " permite maximo " + plan.getMaxProducts() + " productos"
            );
        }
    }

    public void validateCanCreateCategory(User owner, long currentCategoryCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (plan.getMaxCategories() != -1 && currentCategoryCount >= plan.getMaxCategories()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " permite maximo " + plan.getMaxCategories() + " categorias"
            );
        }
    }

    public void validateCanInviteMember(User owner, long currentMemberCount) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (plan.getMaxEmployees() != -1 && currentMemberCount >= plan.getMaxEmployees()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " permite maximo " + plan.getMaxEmployees() + " empleados"
            );
        }
    }

    public void validateCanAccessReports(User owner) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (!plan.isReportsEnabled()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " no incluye reportes. Actualiza a PRO o ELITE."
            );
        }
    }

    public void validateCanAccessAnalytics(User owner) {
        validatePlanNotExpired(owner);
        Plan plan = getPlan(owner);
        if (!plan.isReportsEnabled()) {
            throw new PlanLimitExceededException(
                    "El plan " + plan.getType() + " no incluye analitica. Actualiza a PRO o ELITE."
            );
        }
    }

    public User resolveOwner(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));
        return userRepository.findById(business.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Owner not found for business: " + businessId));
    }

    private Plan getPlan(User owner) {
        return planRepository.findByType(owner.getPlan())
                .orElseThrow(() -> new NotFoundException("Plan not found: " + owner.getPlan()));
    }
}
