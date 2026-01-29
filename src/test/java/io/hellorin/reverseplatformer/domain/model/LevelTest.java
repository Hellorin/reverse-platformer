package io.hellorin.reverseplatformer.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LevelTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void shouldCreateAllLevels(int levelNumber) {
        Level level = Level.createLevel(levelNumber);

        assertThat(level.getLevelNumber()).isEqualTo(levelNumber);
        assertThat(level.getPlatforms()).isNotEmpty();
        assertThat(level.getSpawnPoint()).isNotNull();
        assertThat(level.getGoal()).isNotNull();
    }

    @Test
    void shouldHaveIncreasingDifficulty() {
        Level level1 = Level.createLevel(1);
        Level level5 = Level.createLevel(5);

        assertThat(level5.getRunnerSpeedMultiplier()).isGreaterThan(level1.getRunnerSpeedMultiplier());
        assertThat(level5.getStartingPoints()).isLessThan(level1.getStartingPoints());
    }

    @Test
    void shouldDefaultToMaxLevelForInvalidNumber() {
        Level level = Level.createLevel(999);

        assertThat(level.getLevelNumber()).isEqualTo(5);
    }

    @Test
    void shouldAddPlatform() {
        Level level = new Level(1, 800, 450, new Vector2D(0, 0), new Rectangle(700, 300, 40, 80));
        Platform platform = new Platform("p1", 0, 400, 200, 20);

        level.addPlatform(platform);

        assertThat(level.getPlatforms()).hasSize(1);
        assertThat(level.getPlatforms().get(0).getId()).isEqualTo("p1");
    }

    @Test
    void shouldReturnDefensiveCopyOfPlatforms() {
        Level level = Level.createLevel(1);

        level.getPlatforms().clear();

        assertThat(level.getPlatforms()).isNotEmpty();
    }
}
