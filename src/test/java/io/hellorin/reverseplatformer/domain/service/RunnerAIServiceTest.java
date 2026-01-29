package io.hellorin.reverseplatformer.domain.service;

import io.hellorin.reverseplatformer.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerAIServiceTest {

    private RunnerAIService runnerAIService;
    private Runner runner;
    private List<Platform> platforms;
    private List<Trap> traps;

    @BeforeEach
    void setUp() {
        runnerAIService = new RunnerAIService();
        runner = new Runner(100, 100, 1.0);
        platforms = new ArrayList<>();
        traps = new ArrayList<>();
    }

    @Test
    void shouldNotThinkWhenRunnerIsDead() {
        runner.land(100);
        runner.die();
        double velocityY = runner.getVelocity().y();

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isEqualTo(velocityY);
    }

    @Test
    void shouldNotThinkWhenRunnerIsInAir() {
        assertThat(runner.isOnGround()).isFalse();
        double velocityY = runner.getVelocity().y();

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isEqualTo(velocityY);
    }

    @Test
    void shouldJumpWhenApproachingGap() {
        runner.land(100);
        assertThat(runner.isOnGround()).isTrue();

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isNegative();
    }

    @Test
    void shouldNotJumpWhenPlatformAhead() {
        runner.land(100);
        Rectangle bounds = runner.getBounds();
        platforms.add(new Platform("p1", (int) bounds.right(), 100, 200, 20));

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.isOnGround()).isTrue();
    }

    @Test
    void shouldJumpWhenTrapAhead() {
        runner.land(100);
        Rectangle bounds = runner.getBounds();
        platforms.add(new Platform("p1", (int) bounds.right(), 100, 200, 20));
        traps.add(new Trap("spike", TrapType.SPIKE, (int) bounds.right() + 10, 80));

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isNegative();
    }

    @Test
    void shouldNotJumpOverBouncePad() {
        runner.land(100);
        Rectangle bounds = runner.getBounds();
        platforms.add(new Platform("p1", (int) bounds.right(), 100, 200, 20));
        traps.add(new Trap("bounce", TrapType.BOUNCE_PAD, (int) bounds.right() + 10, 80));

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.isOnGround()).isTrue();
    }

    @Test
    void shouldJumpToHigherPlatform() {
        runner.land(200);
        Rectangle bounds = runner.getBounds();
        platforms.add(new Platform("current", (int) bounds.x(), 200, 100, 20));
        platforms.add(new Platform("higher", (int) bounds.right() + 20, 120, 100, 20));

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isNegative();
    }

    @Test
    void shouldIgnoreInactivePlatforms() {
        runner.land(100);
        Rectangle bounds = runner.getBounds();
        Platform inactivePlatform = new Platform("p1", (int) bounds.right(), 100, 200, 20);
        inactivePlatform.setActive(false);
        platforms.add(inactivePlatform);

        runnerAIService.think(runner, platforms, traps);

        assertThat(runner.getVelocity().y()).isNegative();
    }
}
