package com.mezo.pos.plan.domain.entity;

import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType type;

    @Column(name = "max_tables", nullable = false)
    private int maxTables;

    @Column(name = "max_employees", nullable = false)
    private int maxEmployees;

    @Column(name = "max_categories", nullable = false)
    private int maxCategories;

    @Column(name = "max_products", nullable = false)
    private int maxProducts;

    @Column(name = "max_businesses", nullable = false)
    private int maxBusinesses;

    @Column(name = "reports_enabled", nullable = false)
    private boolean reportsEnabled;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    })
    private Money price;

    @Column(name = "trial_days", nullable = false)
    private int trialDays = 0;
}
