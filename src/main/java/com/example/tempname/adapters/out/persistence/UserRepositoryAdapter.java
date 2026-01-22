package com.example.tempname.adapters.out.persistence;

import com.example.tempname.application.ports.out.UserRepository;
import com.example.tempname.domain.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final ConcurrentMap<UUID, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        storage.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        return storage.containsKey(id);
    }
}
