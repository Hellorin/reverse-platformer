package io.hellorin.reverseplatformer.domain.model;

public enum TrapType {
    SPIKE(50, "Instant kill"),
    BOUNCE_PAD(20, "Launches runner upward"),
    SLOW_ZONE(15, "Reduces runner speed");

    private final int cost;
    private final String description;

    TrapType(int cost, String description) {
        this.cost = cost;
        this.description = description;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }
}
