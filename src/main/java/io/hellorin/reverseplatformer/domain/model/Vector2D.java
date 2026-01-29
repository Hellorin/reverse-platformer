package io.hellorin.reverseplatformer.domain.model;

public record Vector2D(double x, double y) {

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public static Vector2D zero() {
        return new Vector2D(0, 0);
    }
}
