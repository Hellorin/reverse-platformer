package io.hellorin.reverseplatformer.domain.model;

public class Trap {
    private final String id;
    private final TrapType type;
    private final Rectangle bounds;

    public Trap(String id, TrapType type, double x, double y) {
        this.id = id;
        this.type = type;
        this.bounds = new Rectangle(x, y, 30, 30);
    }

    public String getId() {
        return id;
    }

    public TrapType getType() {
        return type;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
