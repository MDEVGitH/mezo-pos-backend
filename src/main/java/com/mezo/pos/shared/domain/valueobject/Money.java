package com.mezo.pos.shared.domain.valueobject;

import com.mezo.pos.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
public class Money {

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Amount must be >= 0");
        }
        if (currency == null) {
            throw new DomainException("Currency is required");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public Money(long amount, Currency currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    private void assertSameCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new DomainException("Cannot operate on different currencies: " + this.currency + " vs " + other.currency);
        }
    }
}
