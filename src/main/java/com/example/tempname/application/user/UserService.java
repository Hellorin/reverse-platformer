package com.example.tempname.application.user;

import com.example.tempname.application.ports.in.UserUseCase;
import com.example.tempname.application.ports.out.UserRepository;
import com.example.tempname.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        User user = new User(UUID.randomUUID(), email, name);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
    }
}
