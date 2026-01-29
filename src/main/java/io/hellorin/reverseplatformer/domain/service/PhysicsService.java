package io.hellorin.reverseplatformer.domain.service;

import io.hellorin.reverseplatformer.domain.model.*;

import java.util.List;

public class PhysicsService {

    public void update(Runner runner, List<Platform> platforms, double deltaTime) {
        runner.update(deltaTime);
        handlePlatformCollisions(runner, platforms);
    }

    private void handlePlatformCollisions(Runner runner, List<Platform> platforms) {
        Rectangle runnerBounds = runner.getBounds();

        for (Platform platform : platforms) {
            if (!platform.isActive()) continue;

            Rectangle platformBounds = platform.getBounds();

            if (isLandingOnPlatform(runnerBounds, platformBounds, runner.getVelocity())) {
                runner.land(platformBounds.y());
                return;
            }
        }
    }

    private boolean isLandingOnPlatform(Rectangle runner, Rectangle platform, Vector2D velocity) {
        if (velocity.y() <= 0) return false;

        boolean horizontalOverlap = runner.right() > platform.x() && runner.x() < platform.right();
        double runnerFeet = runner.bottom();
        boolean verticalContact = runnerFeet >= platform.y() && runnerFeet <= platform.y() + 20;

        return horizontalOverlap && verticalContact;
    }

    public boolean checkTrapCollision(Runner runner, Trap trap) {
        return runner.getBounds().intersects(trap.getBounds());
    }

    public boolean checkGoalReached(Runner runner, Rectangle goal) {
        return runner.getBounds().intersects(goal);
    }

    public boolean checkFellOffMap(Runner runner, double mapHeight) {
        return runner.getPosition().y() > mapHeight + 50;
    }

    public boolean checkOutOfBounds(Runner runner, double mapWidth) {
        return runner.getPosition().x() > mapWidth + 50;
    }
}
