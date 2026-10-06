package com.arecaharvest.controller;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.service.UserService;
import com.arecaharvest.service.UserService.UserSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User account management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all system users")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummary>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserSummary>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user profile and roles (Admin only)")
    public ResponseEntity<ApiResponse<UserSummary>> update(
            @PathVariable Long id,
            @RequestBody UserService.UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }
}
