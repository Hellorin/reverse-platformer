package io.hellorin.reverseplatformer.application.ports.in;

import io.hellorin.reverseplatformer.domain.model.TrapType;

public interface TrapUseCase {
    void placeTrap(TrapType type, double x, double y);
}
