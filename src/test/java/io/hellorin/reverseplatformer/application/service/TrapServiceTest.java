package io.hellorin.reverseplatformer.application.service;

import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.domain.model.GameState;
import io.hellorin.reverseplatformer.domain.model.TrapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrapServiceTest {

    private TrapService trapService;
    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        trapService = new TrapService(session);
    }

    @Test
    void shouldPlaceTrap() {
        session.createNewGame();

        trapService.placeTrap(TrapType.SPIKE, 100, 100);

        assertThat(session.getGameState().getTraps()).hasSize(1);
    }

    @Test
    void shouldDeductPointsWhenPlacingTrap() {
        session.createNewGame();
        int initialPoints = session.getGameState().getPlayerPoints();

        trapService.placeTrap(TrapType.SPIKE, 100, 100);

        assertThat(session.getGameState().getPlayerPoints())
                .isEqualTo(initialPoints - TrapType.SPIKE.getCost());
    }

    @Test
    void shouldNotPlaceTrapWhenGameNotRunning() {
        // Don't start the game
        trapService.placeTrap(TrapType.SPIKE, 100, 100);

        assertThat(session.getGameState()).isNull();
    }

    @Test
    void shouldNotPlaceTrapWhenGameEnded() {
        session.createNewGame();
        session.getGameState().setStatus(GameState.Status.PLAYER_WINS);

        trapService.placeTrap(TrapType.SPIKE, 100, 100);

        assertThat(session.getGameState().getTraps()).isEmpty();
    }

    @Test
    void shouldNotPlaceTrapWhenCannotAfford() {
        session.createNewGame();
        // Drain all points
        while (session.getGameState().canAffordTrap(TrapType.SPIKE)) {
            trapService.placeTrap(TrapType.SPIKE, 100, 100);
        }
        int trapsAfterDraining = session.getGameState().getTraps().size();

        trapService.placeTrap(TrapType.SPIKE, 200, 200);

        assertThat(session.getGameState().getTraps()).hasSize(trapsAfterDraining);
    }

    @Test
    void shouldPlaceSlowZoneTrap() {
        session.createNewGame();

        trapService.placeTrap(TrapType.SLOW_ZONE, 150, 150);

        assertThat(session.getGameState().getTraps()).hasSize(1);
        assertThat(session.getGameState().getTraps().get(0).getType()).isEqualTo(TrapType.SLOW_ZONE);
    }

    @Test
    void shouldPlaceBouncePadTrap() {
        session.createNewGame();

        trapService.placeTrap(TrapType.BOUNCE_PAD, 200, 200);

        assertThat(session.getGameState().getTraps()).hasSize(1);
        assertThat(session.getGameState().getTraps().get(0).getType()).isEqualTo(TrapType.BOUNCE_PAD);
    }
}
