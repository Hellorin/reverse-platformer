package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.domain.model.GameState;
import io.hellorin.reverseplatformer.domain.model.Runner;
import io.hellorin.reverseplatformer.domain.model.Trap;
import io.hellorin.reverseplatformer.domain.model.TrapType;
import io.hellorin.reverseplatformer.domain.service.PhysicsService;
import io.hellorin.reverseplatformer.domain.service.RunnerAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameServiceTest {

    private GameService gameService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        PhysicsService physicsService = new PhysicsService();
        RunnerAIService runnerAIService = new RunnerAIService();
        gameService = new GameService(session, physicsService, runnerAIService);
    }

    @Test
    void shouldStartGame() {
        gameService.startGame();

        assertThat(gameService.getGameState()).isNotNull();
        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameState.Status.RUNNING);
    }

    @Test
    void shouldUpdateGameState() {
        gameService.startGame();
        double initialTime = gameService.getGameState().getElapsedTime();

        gameService.update(0.016);

        assertThat(gameService.getGameState().getElapsedTime()).isGreaterThan(initialTime);
    }

    @Test
    void shouldNotUpdateWhenNotRunning() {
        // Don't start the game
        gameService.update(0.016);

        assertThat(gameService.getGameState()).isNull();
    }

    @Test
    void shouldMoveRunnerOnUpdate() {
        gameService.startGame();
        Runner runner = gameService.getGameState().getRunner();
        double initialX = runner.getPosition().x();

        gameService.update(0.016);

        assertThat(runner.getPosition().x()).isGreaterThan(initialX);
    }

    @Test
    void shouldSetPlayerWinsWhenRunnerDies() {
        gameService.startGame();
        gameService.getGameState().getRunner().die();

        gameService.update(0.016);

        assertThat(gameService.getGameState().getStatus()).isEqualTo(GameState.Status.PLAYER_WINS);
    }

    @Test
    void shouldApplySpikeTrapEffect() {
        gameService.startGame();
        GameState gameState = gameService.getGameState();
        Runner runner = gameState.getRunner();
        int initialPoints = gameState.getPlayerPoints();

        // Place a spike trap at runner's position
        Trap spike = new Trap("spike", TrapType.SPIKE, runner.getPosition().x(), runner.getPosition().y());
        gameState.addTrap(spike);

        gameService.update(0.016);

        assertThat(runner.isAlive()).isFalse();
        assertThat(gameState.getPlayerPoints()).isGreaterThan(initialPoints);
    }

    @Test
    void shouldApplySlowZoneEffect() {
        gameService.startGame();
        GameState gameState = gameService.getGameState();
        Runner runner = gameState.getRunner();

        // Place a slow zone at runner's position
        Trap slowZone = new Trap("slow", TrapType.SLOW_ZONE, runner.getPosition().x(), runner.getPosition().y());
        gameState.addTrap(slowZone);

        gameService.update(0.016);

        assertThat(runner.getSpeedMultiplier()).isEqualTo(0.5);
    }
}
