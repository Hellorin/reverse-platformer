package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.application.ports.in.LevelUseCase;

public class LevelService implements LevelUseCase {

    private final GameSession session;

    public LevelService(GameSession session) {
        this.session = session;
    }

    @Override
    public void restartLevel() {
        session.createNewGame();
    }

    @Override
    public void nextLevel() {
        session.advanceLevel();
        session.createNewGame();
    }

    @Override
    public int getCurrentLevel() {
        return session.getCurrentLevel();
    }

    @Override
    public int getMaxLevel() {
        return session.getMaxLevel();
    }
}
