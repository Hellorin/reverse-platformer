package io.hellorin.reverseplatformer.application.ports.in;

public interface LevelUseCase {
    void restartLevel();
    void nextLevel();
    int getCurrentLevel();
    int getMaxLevel();
}
