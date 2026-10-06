package com.arecaharvest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthResponse {
    private final Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final List<String> roles;
    private final String token;
    private final String tokenType;
    private final long expiresIn;
}
