package io.hellorin.reverseplatformer.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private final int levelNumber;
    private final double width;
    private final double height;
    private final Vector2D spawnPoint;
    private final Rectangle goal;
    private final List<Platform> platforms;
    private double runnerSpeedMultiplier = 1.0;
    private int startingPoints = 100;

    public Level(int levelNumber, double width, double height, Vector2D spawnPoint, Rectangle goal) {
        this.levelNumber = levelNumber;
        this.width = width;
        this.height = height;
        this.spawnPoint = spawnPoint;
        this.goal = goal;
        this.platforms = new ArrayList<>();
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Vector2D getSpawnPoint() {
        return spawnPoint;
    }

    public Rectangle getGoal() {
        return goal;
    }

    public List<Platform> getPlatforms() {
        return new ArrayList<>(platforms);
    }

    public double getRunnerSpeedMultiplier() {
        return runnerSpeedMultiplier;
    }

    public int getStartingPoints() {
        return startingPoints;
    }

    public static Level createLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> createLevel1();
            case 2 -> createLevel2();
            case 3 -> createLevel3();
            case 4 -> createLevel4();
            case 5 -> createLevel5();
            default -> createLevel5();
        };
    }

    private static Level createLevel1() {
        Level level = new Level(1, 800, 450, new Vector2D(50, 300), new Rectangle(750, 320, 40, 80));
        level.runnerSpeedMultiplier = 1.0;
        level.startingPoints = 150;

        level.addPlatform(new Platform("p1", 0, 400, 250, 20));
        level.addPlatform(new Platform("p2", 300, 400, 200, 20));
        level.addPlatform(new Platform("p3", 550, 400, 250, 20));

        return level;
    }

    private static Level createLevel2() {
        Level level = new Level(2, 800, 450, new Vector2D(50, 300), new Rectangle(750, 320, 40, 80));
        level.runnerSpeedMultiplier = 1.2;
        level.startingPoints = 120;

        level.addPlatform(new Platform("p1", 0, 400, 180, 20));
        level.addPlatform(new Platform("p2", 230, 400, 150, 20));
        level.addPlatform(new Platform("p3", 430, 400, 120, 20));
        level.addPlatform(new Platform("p4", 600, 400, 200, 20));
        level.addPlatform(new Platform("p5", 320, 320, 100, 15));

        return level;
    }

    private static Level createLevel3() {
        Level level = new Level(3, 800, 450, new Vector2D(50, 250), new Rectangle(750, 320, 40, 80));
        level.runnerSpeedMultiplier = 1.4;
        level.startingPoints = 100;

        level.addPlatform(new Platform("p1", 0, 400, 150, 20));
        level.addPlatform(new Platform("p2", 200, 400, 100, 20));
        level.addPlatform(new Platform("p3", 350, 400, 100, 20));
        level.addPlatform(new Platform("p4", 500, 400, 80, 20));
        level.addPlatform(new Platform("p5", 630, 400, 170, 20));

        level.addPlatform(new Platform("p6", 0, 300, 120, 15));
        level.addPlatform(new Platform("p7", 170, 280, 100, 15));
        level.addPlatform(new Platform("p8", 320, 260, 120, 15));
        level.addPlatform(new Platform("p9", 490, 280, 100, 15));
        level.addPlatform(new Platform("p10", 640, 300, 160, 15));

        return level;
    }

    private static Level createLevel4() {
        Level level = new Level(4, 800, 450, new Vector2D(50, 300), new Rectangle(750, 370, 40, 80));
        level.runnerSpeedMultiplier = 1.6;
        level.startingPoints = 80;

        level.addPlatform(new Platform("p1", 0, 400, 100, 20));
        level.addPlatform(new Platform("p2", 150, 380, 70, 15));
        level.addPlatform(new Platform("p3", 270, 360, 70, 15));
        level.addPlatform(new Platform("p4", 390, 380, 70, 15));
        level.addPlatform(new Platform("p5", 510, 400, 80, 20));
        level.addPlatform(new Platform("p6", 640, 400, 160, 20));

        level.addPlatform(new Platform("p7", 180, 300, 60, 12));
        level.addPlatform(new Platform("p8", 350, 280, 80, 12));
        level.addPlatform(new Platform("p9", 520, 300, 60, 12));

        return level;
    }

    private static Level createLevel5() {
        Level level = new Level(5, 800, 450, new Vector2D(30, 280), new Rectangle(760, 370, 35, 80));
        level.runnerSpeedMultiplier = 1.8;
        level.startingPoints = 60;

        level.addPlatform(new Platform("p1", 0, 400, 80, 20));
        level.addPlatform(new Platform("p2", 120, 370, 50, 12));
        level.addPlatform(new Platform("p3", 210, 340, 50, 12));
        level.addPlatform(new Platform("p4", 300, 370, 50, 12));
        level.addPlatform(new Platform("p5", 390, 400, 60, 20));
        level.addPlatform(new Platform("p6", 490, 370, 50, 12));
        level.addPlatform(new Platform("p7", 580, 340, 50, 12));
        level.addPlatform(new Platform("p8", 670, 370, 50, 12));
        level.addPlatform(new Platform("p9", 750, 400, 50, 20));

        level.addPlatform(new Platform("p10", 200, 260, 40, 10));
        level.addPlatform(new Platform("p11", 350, 240, 40, 10));
        level.addPlatform(new Platform("p12", 500, 260, 40, 10));

        return level;
    }
}
