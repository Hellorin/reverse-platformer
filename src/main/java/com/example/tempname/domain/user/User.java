package com.example.tempname.domain.user;

import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String name;

    public User(UUID id, String email, String name) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or blank");
        }
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
