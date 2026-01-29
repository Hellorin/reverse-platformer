package io.hellorin.reverseplatformer.adapters.in.websocket.dto;

import io.hellorin.reverseplatformer.domain.model.*;

import java.util.List;

public record GameStateDto(
        String status,
        int playerPoints,
        double elapsedTime,
        int level,
        double speedMultiplier,
        RunnerDto runner,
        List<PlatformDto> platforms,
        List<TrapDto> traps,
        GoalDto goal
) {
    public record RunnerDto(double x, double y, boolean alive) {}
    public record PlatformDto(String id, double x, double y, double width, double height, boolean active) {}
    public record TrapDto(String id, String type, double x, double y) {}
    public record GoalDto(double x, double y, double width, double height) {}

    public static GameStateDto fromDomain(GameState state) {
        Runner runner = state.getRunner();
        Level level = state.getLevel();

        RunnerDto runnerDto = new RunnerDto(
                runner.getPosition().x(),
                runner.getPosition().y(),
                runner.isAlive()
        );

        List<PlatformDto> platformDtos = level.getPlatforms().stream()
                .map(p -> new PlatformDto(
                        p.getId(),
                        p.getBounds().x(),
                        p.getBounds().y(),
                        p.getBounds().width(),
                        p.getBounds().height(),
                        p.isActive()
                ))
                .toList();

        List<TrapDto> trapDtos = state.getTraps().stream()
                .map(t -> new TrapDto(
                        t.getId(),
                        t.getType().name(),
                        t.getBounds().x(),
                        t.getBounds().y()
                ))
                .toList();

        GoalDto goalDto = new GoalDto(
                level.getGoal().x(),
                level.getGoal().y(),
                level.getGoal().width(),
                level.getGoal().height()
        );

        return new GameStateDto(
                state.getStatus().name(),
                state.getPlayerPoints(),
                state.getElapsedTime(),
                level.getLevelNumber(),
                level.getRunnerSpeedMultiplier(),
                runnerDto,
                platformDtos,
                trapDtos,
                goalDto
        );
    }
}
