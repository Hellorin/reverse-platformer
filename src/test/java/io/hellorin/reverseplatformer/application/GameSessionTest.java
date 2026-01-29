package io.hellorin.reverseplatformer.application;

import io.hellorin.reverseplatformer.domain.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
    }

    @Test
    void shouldStartAtLevelOne() {
        assertThat(session.getCurrentLevel()).isEqualTo(1);
    }

    @Test
    void shouldCreateNewGame() {
        session.createNewGame();

        assertThat(session.getGameState()).isNotNull();
        assertThat(session.getGameState().getStatus()).isEqualTo(GameState.Status.RUNNING);
    }

    @Test
    void shouldBeRunningAfterGameCreation() {
        session.createNewGame();

        assertThat(session.isRunning()).isTrue();
    }

    @Test
    void shouldNotBeRunningBeforeGameCreation() {
        assertThat(session.isRunning()).isFalse();
    }

    @Test
    void shouldAdvanceLevel() {
        session.advanceLevel();

        assertThat(session.getCurrentLevel()).isEqualTo(2);
    }

    @Test
    void shouldNotAdvanceBeyondMaxLevel() {
        for (int i = 0; i < 10; i++) {
            session.advanceLevel();
        }

        assertThat(session.getCurrentLevel()).isEqualTo(session.getMaxLevel());
    }

    @Test
    void shouldReturnMaxLevel() {
        assertThat(session.getMaxLevel()).isEqualTo(5);
    }

    @Test
    void shouldCreateGameAtCurrentLevel() {
        session.advanceLevel();
        session.advanceLevel();
        session.createNewGame();

        assertThat(session.getGameState().getLevel().getLevelNumber()).isEqualTo(3);
    }
}
