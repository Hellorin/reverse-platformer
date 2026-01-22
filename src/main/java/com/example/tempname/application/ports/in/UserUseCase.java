package com.example.tempname.application.ports.in;

import com.example.tempname.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserUseCase {

    User createUser(String email, String name);

    Optional<User> findUserById(UUID id);

    List<User> findAllUsers();

    void deleteUser(UUID id);
}
