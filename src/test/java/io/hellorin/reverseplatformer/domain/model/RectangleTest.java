package io.hellorin.reverseplatformer.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RectangleTest {

    @Test
    void shouldDetectIntersection() {
        Rectangle r1 = new Rectangle(0, 0, 100, 100);
        Rectangle r2 = new Rectangle(50, 50, 100, 100);

        assertThat(r1.intersects(r2)).isTrue();
    }

    @Test
    void shouldDetectNoIntersection() {
        Rectangle r1 = new Rectangle(0, 0, 50, 50);
        Rectangle r2 = new Rectangle(100, 100, 50, 50);

        assertThat(r1.intersects(r2)).isFalse();
    }

    @Test
    void shouldCalculateRightEdge() {
        Rectangle r = new Rectangle(10, 20, 100, 50);

        assertThat(r.right()).isEqualTo(110);
    }

    @Test
    void shouldCalculateBottomEdge() {
        Rectangle r = new Rectangle(10, 20, 100, 50);

        assertThat(r.bottom()).isEqualTo(70);
    }

    @Test
    void shouldCreateNewRectangleWithPosition() {
        Rectangle r = new Rectangle(10, 20, 100, 50);

        Rectangle moved = r.withPosition(30, 40);

        assertThat(moved.x()).isEqualTo(30);
        assertThat(moved.y()).isEqualTo(40);
        assertThat(moved.width()).isEqualTo(100);
        assertThat(moved.height()).isEqualTo(50);
    }
}
