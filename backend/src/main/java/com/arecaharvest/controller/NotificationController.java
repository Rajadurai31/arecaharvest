package com.arecaharvest.controller;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.dto.response.ApiResponse;
import com.arecaharvest.dto.response.PagedResponse;
import com.arecaharvest.entity.Notification;
import com.arecaharvest.repository.NotificationRepository;
import com.arecaharvest.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "In-app notification management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<Notification>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(notificationRepository
                        .findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable))));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.countByRecipientIdAndReadFalse(currentUser.getId())));
    }

    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> markAllRead(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        int count = notificationRepository.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count, count + " notifications marked as read"));
    }

    @PostMapping("/dispatch-now")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger all notification jobs (admin only)")
    public ResponseEntity<ApiResponse<String>> dispatchNow() {
        notificationService.runDailyNotifications();
        return ResponseEntity.ok(ApiResponse.success("ok", "Notifications dispatched"));
    }
}
