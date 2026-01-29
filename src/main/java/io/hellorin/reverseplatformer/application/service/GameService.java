package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.application.ports.in.GameUseCase;
import io.hellorin.reverseplatformer.domain.model.*;
import io.hellorin.reverseplatformer.domain.service.PhysicsService;
import io.hellorin.reverseplatformer.domain.service.RunnerAIService;

public class GameService implements GameUseCase {

    private final GameSession session;
    private final PhysicsService physicsService;
    private final RunnerAIService runnerAIService;

    public GameService(GameSession session, PhysicsService physicsService, RunnerAIService runnerAIService) {
        this.session = session;
        this.physicsService = physicsService;
        this.runnerAIService = runnerAIService;
    }

    @Override
    public void startGame() {
        session.createNewGame();
    }

    @Override
    public void update(double deltaTime) {
        if (!session.isRunning()) {
            return;
        }

        GameState gameState = session.getGameState();
        Runner runner = gameState.getRunner();
        Level level = gameState.getLevel();

        gameState.incrementTime(deltaTime);
        runnerAIService.think(runner, level.getPlatforms(), gameState.getTraps());
        physicsService.update(runner, level.getPlatforms(), deltaTime);
        handleTrapCollisions(gameState);
        checkGameOver(gameState);
    }

    @Override
    public GameState getGameState() {
        return session.getGameState();
    }

    private void handleTrapCollisions(GameState gameState) {
        Runner runner = gameState.getRunner();

        for (Trap trap : gameState.getTraps()) {
            if (physicsService.checkTrapCollision(runner, trap)) {
                applyTrapEffect(runner, trap, gameState);
            }
        }
    }

    private void applyTrapEffect(Runner runner, Trap trap, GameState gameState) {
        switch (trap.getType()) {
            case SPIKE -> {
                runner.die();
                gameState.addPoints(100);
            }
            case BOUNCE_PAD -> runner.bounce();
            case SLOW_ZONE -> runner.setSpeedMultiplier(0.5);
        }
    }

    private void checkGameOver(GameState gameState) {
        Runner runner = gameState.getRunner();
        Level level = gameState.getLevel();

        if (!runner.isAlive()) {
            gameState.setStatus(GameState.Status.PLAYER_WINS);
            return;
        }

        if (physicsService.checkFellOffMap(runner, level.getHeight())) {
            gameState.setStatus(GameState.Status.PLAYER_WINS);
            return;
        }

        if (physicsService.checkGoalReached(runner, level.getGoal())) {
            gameState.setStatus(GameState.Status.RUNNER_WINS);
            return;
        }

        if (physicsService.checkOutOfBounds(runner, level.getWidth())) {
            gameState.setStatus(GameState.Status.RUNNER_WINS);
        }
    }
}
