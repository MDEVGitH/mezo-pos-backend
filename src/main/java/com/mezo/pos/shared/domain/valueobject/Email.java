package com.mezo.pos.shared.domain.valueobject;

import com.mezo.pos.shared.domain.exception.DomainException;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor
public class Email {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private String value;

    public Email(String value) {
        if (value == null || !EMAIL_REGEX.matcher(value).matches()) {
            throw new DomainException("Invalid email: " + value);
        }
        this.value = value.toLowerCase().trim();
    }
}
