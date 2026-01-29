package io.hellorin.reverseplatformer.domain.model;

public class Runner {
    private static final double WIDTH = 20;
    private static final double HEIGHT = 30;
    private static final double BASE_SPEED = 150;
    private static final double JUMP_FORCE = -350;
    private static final double GRAVITY = 800;

    private Vector2D position;
    private Vector2D velocity;
    private boolean onGround;
    private boolean alive;
    private double speedMultiplier;
    private final double baseSpeedMultiplier;

    public Runner(double startX, double startY, double levelSpeedMultiplier) {
        this.position = new Vector2D(startX, startY);
        this.baseSpeedMultiplier = levelSpeedMultiplier;
        this.velocity = new Vector2D(BASE_SPEED * levelSpeedMultiplier, 0);
        this.onGround = false;
        this.alive = true;
        this.speedMultiplier = 1.0;
    }

    public void update(double deltaTime) {
        if (!alive) return;

        double gravity = GRAVITY * deltaTime;
        velocity = new Vector2D(BASE_SPEED * baseSpeedMultiplier * speedMultiplier, velocity.y() + gravity);
        position = position.add(velocity.multiply(deltaTime));
        onGround = false;
    }

    public void jump() {
        if (onGround && alive) {
            velocity = new Vector2D(velocity.x(), JUMP_FORCE);
            onGround = false;
        }
    }

    public void bounce() {
        velocity = new Vector2D(velocity.x(), JUMP_FORCE * 1.5);
        onGround = false;
    }

    public void land(double platformY) {
        position = new Vector2D(position.x(), platformY - HEIGHT);
        velocity = new Vector2D(velocity.x(), 0);
        onGround = true;
    }

    public void die() {
        alive = false;
        velocity = Vector2D.zero();
    }

    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    public void resetSpeedMultiplier() {
        this.speedMultiplier = 1.0;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x(), position.y(), WIDTH, HEIGHT);
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
