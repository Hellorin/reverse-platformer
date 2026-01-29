package io.hellorin.reverseplatformer.domain.model;

public record Rectangle(double x, double y, double width, double height) {

    public boolean intersects(Rectangle other) {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }

    public double right() {
        return x + width;
    }

    public double bottom() {
        return y + height;
    }

    public Rectangle withPosition(double newX, double newY) {
        return new Rectangle(newX, newY, width, height);
    }
}
