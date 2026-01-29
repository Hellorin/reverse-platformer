package io.hellorin.reverseplatformer.domain.service;

import io.hellorin.reverseplatformer.domain.model.*;

import java.util.List;

public class RunnerAIService {

    private static final double EDGE_DETECTION_DISTANCE = 25;
    private static final double OBSTACLE_DETECTION_DISTANCE = 30;

    public void think(Runner runner, List<Platform> platforms, List<Trap> traps) {
        if (!runner.isAlive() || !runner.isOnGround()) return;

        if (isApproachingGap(runner, platforms)) {
            runner.jump();
            return;
        }

        if (isTrapAhead(runner, traps)) {
            runner.jump();
            return;
        }

        if (shouldJumpToHigherPlatform(runner, platforms)) {
            runner.jump();
        }
    }

    private boolean isApproachingGap(Runner runner, List<Platform> platforms) {
        Rectangle runnerBounds = runner.getBounds();
        double checkX = runnerBounds.right() + EDGE_DETECTION_DISTANCE;
        double checkY = runnerBounds.bottom() + 10;

        for (Platform platform : platforms) {
            if (!platform.isActive()) continue;

            Rectangle bounds = platform.getBounds();
            if (checkX >= bounds.x() && checkX <= bounds.right() &&
                checkY >= bounds.y() && checkY <= bounds.bottom()) {
                return false;
            }
        }

        return true;
    }

    private boolean isTrapAhead(Runner runner, List<Trap> traps) {
        Rectangle runnerBounds = runner.getBounds();

        for (Trap trap : traps) {
            Rectangle trapBounds = trap.getBounds();

            double distanceAhead = trapBounds.x() - runnerBounds.right();
            boolean isAhead = distanceAhead > 0 && distanceAhead < OBSTACLE_DETECTION_DISTANCE;
            boolean sameLevel = Math.abs(trapBounds.y() - runnerBounds.y()) < 50;

            if (isAhead && sameLevel && trap.getType() != TrapType.BOUNCE_PAD) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldJumpToHigherPlatform(Runner runner, List<Platform> platforms) {
        Rectangle runnerBounds = runner.getBounds();

        for (Platform platform : platforms) {
            if (!platform.isActive()) continue;

            Rectangle bounds = platform.getBounds();

            double distanceAhead = bounds.x() - runnerBounds.right();
            double heightDiff = runnerBounds.y() - bounds.y();

            boolean isAhead = distanceAhead > -20 && distanceAhead < 60;
            boolean isAbove = heightDiff > 30 && heightDiff < 120;

            if (isAhead && isAbove) {
                return true;
            }
        }

        return false;
    }
}
