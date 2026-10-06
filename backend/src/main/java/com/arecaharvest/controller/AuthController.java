package com.arecaharvest.controller;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.request.LoginRequest;
import com.arecaharvest.dto.request.RegisterRequest;
import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.AuthResponse;
import com.arecaharvest.entity.User;
import com.arecaharvest.repository.UserRepository;
import com.arecaharvest.service.AuthService;
import com.arecaharvest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Login, register, and user profile endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Register a new user (Admin only)")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @Operation(summary = "Get current authenticated user profile")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new com.arecaharvest.exception.ResourceNotFoundException("User", "id", currentUser.getId()));

        var profile = new UserProfileResponse(
                user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getPhone(), user.getLastLoginAt(),
                user.getRoles().stream().map(r -> r.getName()).toList()
        );
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /** Inline record — avoids a separate file for a simple projection */
    public record UserProfileResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        java.time.LocalDateTime lastLoginAt,
        java.util.List<String> roles
    ) {}

    @Operation(summary = "Change own password")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody UserService.ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(),
                request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
