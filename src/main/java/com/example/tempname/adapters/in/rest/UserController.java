package com.example.tempname.adapters.in.rest;

import com.example.tempname.adapters.in.rest.dto.CreateUserRequest;
import com.example.tempname.adapters.in.rest.dto.UserResponse;
import com.example.tempname.application.ports.in.UserUseCase;
import com.example.tempname.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        if (userUseCase == null) {
            throw new IllegalArgumentException("UserUseCase cannot be null");
        }
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userUseCase.createUser(request.email(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userUseCase.findUserById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userUseCase.findAllUsers().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
