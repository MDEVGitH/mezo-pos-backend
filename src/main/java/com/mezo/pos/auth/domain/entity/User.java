package com.mezo.pos.auth.domain.entity;

import com.mezo.pos.auth.domain.enums.Role;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.valueobject.Email;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false))
    private Email email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.PRO;

    @Column(name = "plan_started_at")
    private LocalDateTime planStartedAt;

    @Column(name = "plan_expires_at")
    private LocalDateTime planExpiresAt;
}
