package io.hellorin.reverseplatformer.domain.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public enum Status {
        WAITING,
        RUNNING,
        PLAYER_WINS,
        RUNNER_WINS
    }

    private Status status;
    private final Level level;
    private Runner runner;
    private final List<Trap> traps;
    private int playerPoints;
    private double elapsedTime;

    public GameState(Level level) {
        this.level = level;
        this.traps = new ArrayList<>();
        this.playerPoints = level.getStartingPoints();
        this.elapsedTime = 0;
        this.status = Status.WAITING;
        spawnRunner();
    }

    public void spawnRunner() {
        this.runner = new Runner(
            level.getSpawnPoint().x(),
            level.getSpawnPoint().y(),
            level.getRunnerSpeedMultiplier()
        );
    }

    public void start() {
        this.status = Status.RUNNING;
    }

    public void addTrap(Trap trap) {
        if (playerPoints >= trap.getType().getCost()) {
            traps.add(trap);
            playerPoints -= trap.getType().getCost();
        }
    }

    public boolean canAffordTrap(TrapType type) {
        return playerPoints >= type.getCost();
    }

    public void addPoints(int points) {
        this.playerPoints += points;
    }

    public void incrementTime(double deltaTime) {
        this.elapsedTime += deltaTime;
        if (elapsedTime % 0.5 < deltaTime) {
            playerPoints += 1;
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Level getLevel() {
        return level;
    }

    public Runner getRunner() {
        return runner;
    }

    public List<Trap> getTraps() {
        return new ArrayList<>(traps);
    }

    public int getPlayerPoints() {
        return playerPoints;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }
}
