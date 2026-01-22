package com.example.tempname.adapters.in.rest.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name
) {
}
