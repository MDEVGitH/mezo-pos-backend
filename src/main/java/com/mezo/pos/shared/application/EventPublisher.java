package com.mezo.pos.shared.application;

public interface EventPublisher {
    void publish(Object event);
}
