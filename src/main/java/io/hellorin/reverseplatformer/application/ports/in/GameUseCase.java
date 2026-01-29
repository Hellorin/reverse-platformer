package io.hellorin.reverseplatformer.application.ports.in;

import io.hellorin.reverseplatformer.domain.model.GameState;

public interface GameUseCase {
    void startGame();
    void update(double deltaTime);
    GameState getGameState();
}
