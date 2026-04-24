package com.mezo.pos.plan.infrastructure.config;

import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.plan.domain.port.PlanRepository;
import com.mezo.pos.shared.domain.valueobject.Currency;
import com.mezo.pos.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            log.info("Seeding plan data...");

            Plan semilla = Plan.builder()
                    .type(PlanType.SEMILLA)
                    .maxTables(4)
                    .maxEmployees(3)
                    .maxCategories(3)
                    .maxProducts(25)
                    .maxBusinesses(1)
                    .reportsEnabled(false)
                    .price(new Money(39900, Currency.COP))
                    .trialDays(0)
                    .build();

            Plan pro = Plan.builder()
                    .type(PlanType.PRO)
                    .maxTables(-1)
                    .maxEmployees(-1)
                    .maxCategories(-1)
                    .maxProducts(-1)
                    .maxBusinesses(1)
                    .reportsEnabled(true)
                    .price(new Money(99900, Currency.COP))
                    .trialDays(30)
                    .build();

            Plan elite = Plan.builder()
                    .type(PlanType.ELITE)
                    .maxTables(-1)
                    .maxEmployees(-1)
                    .maxCategories(-1)
                    .maxProducts(-1)
                    .maxBusinesses(-1)
                    .reportsEnabled(true)
                    .price(new Money(199900, Currency.COP))
                    .trialDays(0)
                    .build();

            planRepository.save(semilla);
            planRepository.save(pro);
            planRepository.save(elite);

            log.info("Plan data seeded successfully: SEMILLA, PRO, ELITE");
        }
    }
}
