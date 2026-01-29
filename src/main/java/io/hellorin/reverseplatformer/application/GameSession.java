package io.hellorin.reverseplatformer.application;

import io.hellorin.reverseplatformer.domain.model.GameState;
import io.hellorin.reverseplatformer.domain.model.Level;

public class GameSession {

    private static final int MAX_LEVEL = 5;

    private GameState gameState;
    private int currentLevel = 1;

    public void createNewGame() {
        Level level = Level.createLevel(currentLevel);
        this.gameState = new GameState(level);
        this.gameState.start();
    }

    public void advanceLevel() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public boolean isRunning() {
        return gameState != null && gameState.getStatus() == GameState.Status.RUNNING;
    }
}
