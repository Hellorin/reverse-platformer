package io.hellorin.reverseplatformer.domain.service;

import io.hellorin.reverseplatformer.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhysicsServiceTest {

    private PhysicsService physicsService;
    private Runner runner;
    private List<Platform> platforms;

    @BeforeEach
    void setUp() {
        physicsService = new PhysicsService();
        runner = new Runner(100, 100, 1.0);
        platforms = new ArrayList<>();
    }

    @Test
    void shouldUpdateRunnerPosition() {
        double initialX = runner.getPosition().x();

        physicsService.update(runner, platforms, 0.016);

        assertThat(runner.getPosition().x()).isGreaterThan(initialX);
    }

    @Test
    void shouldLandRunnerOnPlatform() {
        runner = new Runner(100, 280, 1.0);
        // Simulate falling
        runner.update(0.1);
        platforms.add(new Platform("p1", 50, 300, 200, 20));

        physicsService.update(runner, platforms, 0.016);

        assertThat(runner.isOnGround()).isTrue();
    }

    @Test
    void shouldDetectTrapCollision() {
        runner = new Runner(100, 100, 1.0);
        Trap trap = new Trap("trap1", TrapType.SPIKE, 100, 100);

        boolean collision = physicsService.checkTrapCollision(runner, trap);

        assertThat(collision).isTrue();
    }

    @Test
    void shouldDetectNoTrapCollision() {
        runner = new Runner(100, 100, 1.0);
        Trap trap = new Trap("trap1", TrapType.SPIKE, 500, 500);

        boolean collision = physicsService.checkTrapCollision(runner, trap);

        assertThat(collision).isFalse();
    }

    @Test
    void shouldDetectGoalReached() {
        runner = new Runner(100, 100, 1.0);
        Rectangle goal = new Rectangle(90, 90, 50, 50);

        boolean reached = physicsService.checkGoalReached(runner, goal);

        assertThat(reached).isTrue();
    }

    @Test
    void shouldDetectGoalNotReached() {
        runner = new Runner(100, 100, 1.0);
        Rectangle goal = new Rectangle(500, 500, 50, 50);

        boolean reached = physicsService.checkGoalReached(runner, goal);

        assertThat(reached).isFalse();
    }

    @Test
    void shouldDetectRunnerFellOffMap() {
        runner = new Runner(100, 600, 1.0);

        boolean fellOff = physicsService.checkFellOffMap(runner, 500);

        assertThat(fellOff).isTrue();
    }

    @Test
    void shouldDetectRunnerNotFellOffMap() {
        runner = new Runner(100, 200, 1.0);

        boolean fellOff = physicsService.checkFellOffMap(runner, 500);

        assertThat(fellOff).isFalse();
    }

    @Test
    void shouldDetectRunnerOutOfBounds() {
        runner = new Runner(900, 100, 1.0);

        boolean outOfBounds = physicsService.checkOutOfBounds(runner, 800);

        assertThat(outOfBounds).isTrue();
    }

    @Test
    void shouldDetectRunnerNotOutOfBounds() {
        runner = new Runner(400, 100, 1.0);

        boolean outOfBounds = physicsService.checkOutOfBounds(runner, 800);

        assertThat(outOfBounds).isFalse();
    }

    @Test
    void shouldIgnoreInactivePlatforms() {
        runner = new Runner(100, 280, 1.0);
        runner.update(0.1);
        Platform platform = new Platform("p1", 50, 300, 200, 20);
        platform.setActive(false);
        platforms.add(platform);

        physicsService.update(runner, platforms, 0.016);

        assertThat(runner.isOnGround()).isFalse();
    }
}
