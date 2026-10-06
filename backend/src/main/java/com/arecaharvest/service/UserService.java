package com.arecaharvest.service;

import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Role;
import com.arecaharvest.entity.User;
import com.arecaharvest.exception.BusinessException;
import com.arecaharvest.exception.ResourceNotFoundException;
import com.arecaharvest.repository.RoleRepository;
import com.arecaharvest.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public PagedResponse<UserSummary> listUsers(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return PagedResponse.of(userRepository.findAll(pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public UserSummary getById(Long id) {
        return toSummary(findOrThrow(id));
    }

    @Transactional
    public UserSummary update(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(req.getRoles()));
        }
        User saved = userRepository.save(user);
        auditLog.log("UPDATE", "USER", id, "User updated: " + saved.getUsername());
        return toSummary(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
        auditLog.log("DEACTIVATE", "USER", id, "User deactivated: " + user.getUsername());
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException("New password must be at least 8 characters");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLog.log("CHANGE_PASSWORD", "USER", userId, "Password changed for " + user.getUsername());
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Set<Role> resolveRoles(List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            roleRepository.findByName(normalized).ifPresent(roles::add);
        }
        return roles;
    }

    private UserSummary toSummary(User u) {
        List<String> roles = u.getRoles().stream().map(Role::getName).toList();
        return new UserSummary(u.getId(), u.getUsername(), u.getFullName(),
                u.getEmail(), u.getPhone(), u.isActive(), u.getLastLoginAt(), roles);
    }

    public record UserSummary(Long id, String username, String fullName, String email,
                               String phone, boolean active,
                               java.time.LocalDateTime lastLoginAt, List<String> roles) {}

    @Getter @Setter
    public static class UpdateUserRequest {
        private String fullName; private String email; private String phone;
        private List<String> roles;
    }

    @Getter @Setter
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }
}
