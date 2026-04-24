package com.mezo.pos.shared.domain.valueobject;

import com.mezo.pos.shared.domain.exception.DomainException;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class PhoneNumber {

    private String value;

    public PhoneNumber(String value) {
        if (value == null || value.length() < 7 || value.length() > 15) {
            throw new DomainException("Invalid phone number");
        }
        this.value = value;
    }
}
