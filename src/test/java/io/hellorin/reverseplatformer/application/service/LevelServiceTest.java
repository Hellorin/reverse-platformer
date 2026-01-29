package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.domain.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LevelServiceTest {

    private LevelService levelService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        levelService = new LevelService(session);
    }

    @Test
    void shouldGetCurrentLevel() {
        assertThat(levelService.getCurrentLevel()).isEqualTo(1);
    }

    @Test
    void shouldGetMaxLevel() {
        assertThat(levelService.getMaxLevel()).isEqualTo(5);
    }

    @Test
    void shouldRestartLevel() {
        session.createNewGame();
        session.getGameState().setStatus(GameState.Status.PLAYER_WINS);

        levelService.restartLevel();

        assertThat(session.getGameState().getStatus()).isEqualTo(GameState.Status.RUNNING);
        assertThat(levelService.getCurrentLevel()).isEqualTo(1);
    }

    @Test
    void shouldAdvanceToNextLevel() {
        session.createNewGame();

        levelService.nextLevel();

        assertThat(levelService.getCurrentLevel()).isEqualTo(2);
        assertThat(session.getGameState().getStatus()).isEqualTo(GameState.Status.RUNNING);
    }

    @Test
    void shouldNotAdvanceBeyondMaxLevel() {
        for (int i = 0; i < 10; i++) {
            session.advanceLevel();
        }

        assertThat(levelService.getCurrentLevel()).isEqualTo(5);
    }

    @Test
    void shouldCreateNewGameOnNextLevel() {
        session.createNewGame();
        session.getGameState().addPoints(1000);
        int pointsBeforeNextLevel = session.getGameState().getPlayerPoints();

        levelService.nextLevel();

        // New game should have fresh points based on level
        assertThat(session.getGameState().getPlayerPoints()).isNotEqualTo(pointsBeforeNextLevel);
    }
}
