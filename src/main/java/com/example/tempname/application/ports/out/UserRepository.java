package com.example.tempname.application.ports.out;

import com.example.tempname.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
