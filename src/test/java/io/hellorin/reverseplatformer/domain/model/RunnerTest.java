package io.hellorin.reverseplatformer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    private Runner runner;

    @BeforeEach
    void setUp() {
        runner = new Runner(100, 200, 1.0);
    }

    @Test
    void shouldInitializeAtStartPosition() {
        assertThat(runner.getPosition().x()).isEqualTo(100);
        assertThat(runner.getPosition().y()).isEqualTo(200);
        assertThat(runner.isAlive()).isTrue();
    }

    @Test
    void shouldMoveRightOnUpdate() {
        double initialX = runner.getPosition().x();

        runner.update(0.016); // ~60fps

        assertThat(runner.getPosition().x()).isGreaterThan(initialX);
    }

    @Test
    void shouldApplyGravityOnUpdate() {
        double initialVelocityY = runner.getVelocity().y();

        runner.update(0.016);

        assertThat(runner.getVelocity().y()).isGreaterThan(initialVelocityY);
    }

    @Test
    void shouldJumpWhenOnGround() {
        runner.land(300);
        assertThat(runner.isOnGround()).isTrue();

        runner.jump();

        assertThat(runner.getVelocity().y()).isNegative();
        assertThat(runner.isOnGround()).isFalse();
    }

    @Test
    void shouldNotJumpWhenInAir() {
        assertThat(runner.isOnGround()).isFalse();
        double velocityY = runner.getVelocity().y();

        runner.jump();

        assertThat(runner.getVelocity().y()).isEqualTo(velocityY);
    }

    @Test
    void shouldLandOnPlatform() {
        runner.land(300);

        assertThat(runner.isOnGround()).isTrue();
        assertThat(runner.getVelocity().y()).isEqualTo(0);
    }

    @Test
    void shouldDie() {
        runner.die();

        assertThat(runner.isAlive()).isFalse();
    }

    @Test
    void shouldNotUpdateWhenDead() {
        runner.die();
        Vector2D positionBeforeDeath = runner.getPosition();

        runner.update(0.016);

        assertThat(runner.getPosition()).isEqualTo(positionBeforeDeath);
    }

    @Test
    void shouldBounceHigherThanJump() {
        runner.land(300);
        runner.jump();
        double jumpVelocity = runner.getVelocity().y();

        runner.land(300);
        runner.bounce();
        double bounceVelocity = runner.getVelocity().y();

        assertThat(bounceVelocity).isLessThan(jumpVelocity);
    }

    @Test
    void shouldApplySpeedMultiplier() {
        runner.setSpeedMultiplier(0.5);

        assertThat(runner.getSpeedMultiplier()).isEqualTo(0.5);
    }

    @Test
    void shouldResetSpeedMultiplier() {
        runner.setSpeedMultiplier(0.5);
        runner.resetSpeedMultiplier();

        assertThat(runner.getSpeedMultiplier()).isEqualTo(1.0);
    }
}
