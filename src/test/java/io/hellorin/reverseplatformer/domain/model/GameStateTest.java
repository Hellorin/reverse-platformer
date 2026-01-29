package io.hellorin.reverseplatformer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateTest {

    private GameState gameState;
    private Level level;

    @BeforeEach
    void setUp() {
        level = Level.createLevel(1);
        gameState = new GameState(level);
    }

    @Test
    void shouldInitializeWithWaitingStatus() {
        assertThat(gameState.getStatus()).isEqualTo(GameState.Status.WAITING);
    }

    @Test
    void shouldStartGame() {
        gameState.start();

        assertThat(gameState.getStatus()).isEqualTo(GameState.Status.RUNNING);
    }

    @Test
    void shouldInitializeWithLevelStartingPoints() {
        assertThat(gameState.getPlayerPoints()).isEqualTo(level.getStartingPoints());
    }

    @Test
    void shouldSpawnRunner() {
        assertThat(gameState.getRunner()).isNotNull();
        assertThat(gameState.getRunner().isAlive()).isTrue();
    }

    @Test
    void shouldAddTrapWhenAffordable() {
        int initialPoints = gameState.getPlayerPoints();
        Trap trap = new Trap("trap1", TrapType.SLOW_ZONE, 100, 100);

        gameState.addTrap(trap);

        assertThat(gameState.getTraps()).hasSize(1);
        assertThat(gameState.getPlayerPoints()).isEqualTo(initialPoints - TrapType.SLOW_ZONE.getCost());
    }

    @Test
    void shouldNotAddTrapWhenNotAffordable() {
        // Drain points
        while (gameState.getPlayerPoints() >= TrapType.SPIKE.getCost()) {
            gameState.addTrap(new Trap("t", TrapType.SPIKE, 0, 0));
        }
        int trapsBeforeAttempt = gameState.getTraps().size();

        gameState.addTrap(new Trap("expensive", TrapType.SPIKE, 100, 100));

        assertThat(gameState.getTraps()).hasSize(trapsBeforeAttempt);
    }

    @Test
    void shouldCheckIfCanAffordTrap() {
        assertThat(gameState.canAffordTrap(TrapType.SLOW_ZONE)).isTrue();
        assertThat(gameState.canAffordTrap(TrapType.SPIKE)).isTrue();
    }

    @Test
    void shouldAddPoints() {
        int initialPoints = gameState.getPlayerPoints();

        gameState.addPoints(50);

        assertThat(gameState.getPlayerPoints()).isEqualTo(initialPoints + 50);
    }

    @Test
    void shouldIncrementTime() {
        gameState.incrementTime(0.5);

        assertThat(gameState.getElapsedTime()).isEqualTo(0.5);
    }

    @Test
    void shouldSetStatus() {
        gameState.setStatus(GameState.Status.PLAYER_WINS);

        assertThat(gameState.getStatus()).isEqualTo(GameState.Status.PLAYER_WINS);
    }
}
