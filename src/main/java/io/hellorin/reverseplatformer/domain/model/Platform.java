package io.hellorin.reverseplatformer.domain.model;

public class Platform {
    private final String id;
    private final Rectangle bounds;
    private boolean active;

    public Platform(String id, double x, double y, double width, double height) {
        this.id = id;
        this.bounds = new Rectangle(x, y, width, height);
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
