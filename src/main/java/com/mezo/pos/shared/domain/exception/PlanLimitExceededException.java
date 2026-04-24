package com.mezo.pos.shared.domain.exception;

public class PlanLimitExceededException extends DomainException {

    public PlanLimitExceededException(String message) {
        super(message);
    }
}
