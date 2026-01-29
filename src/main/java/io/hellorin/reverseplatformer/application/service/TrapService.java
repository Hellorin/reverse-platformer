package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.application.ports.in.TrapUseCase;
import io.hellorin.reverseplatformer.domain.model.GameState;
import io.hellorin.reverseplatformer.domain.model.Trap;
import io.hellorin.reverseplatformer.domain.model.TrapType;

public class TrapService implements TrapUseCase {

    private final GameSession session;

    public TrapService(GameSession session) {
        this.session = session;
    }

    @Override
    public void placeTrap(TrapType type, double x, double y) {
        if (!session.isRunning()) {
            return;
        }

        GameState gameState = session.getGameState();

        if (gameState.canAffordTrap(type)) {
            String id = "trap_" + System.currentTimeMillis();
            Trap trap = new Trap(id, type, x, y);
            gameState.addTrap(trap);
        }
    }
}
