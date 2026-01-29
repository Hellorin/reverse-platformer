package io.hellorin.reverseplatformer.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Vector2DTest {

    @Test
    void shouldAddTwoVectors() {
        Vector2D v1 = new Vector2D(10, 20);
        Vector2D v2 = new Vector2D(5, 15);

        Vector2D result = v1.add(v2);

        assertThat(result.x()).isEqualTo(15);
        assertThat(result.y()).isEqualTo(35);
    }

    @Test
    void shouldMultiplyByScalar() {
        Vector2D v = new Vector2D(10, 20);

        Vector2D result = v.multiply(2.5);

        assertThat(result.x()).isEqualTo(25);
        assertThat(result.y()).isEqualTo(50);
    }

    @Test
    void shouldCreateZeroVector() {
        Vector2D zero = Vector2D.zero();

        assertThat(zero.x()).isEqualTo(0);
        assertThat(zero.y()).isEqualTo(0);
    }
}
