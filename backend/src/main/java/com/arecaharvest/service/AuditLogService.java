package com.arecaharvest.service;

import com.arecaharvest.config.security.UserDetailsImpl;
import com.arecaharvest.entity.AuditLog;
import com.arecaharvest.entity.User;
import com.arecaharvest.repository.AuditLogRepository;
import com.arecaharvest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Logs an auditable action asynchronously to avoid blocking business transactions.
     * All financial and operational changes must be logged.
     */
    @Async
    public void log(String action, String entityType, Long entityId,
                    Map<String, Object> previousValue, Map<String, Object> newValue,
                    String description) {
        try {
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .previousValue(previousValue)
                    .newValue(newValue)
                    .description(description)
                    .performedBy(getCurrentUser())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log for action={} entity={}/{}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }

    @Async
    public void log(String action, String entityType, Long entityId, String description) {
        log(action, entityType, entityId, null, null, description);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId()).orElse(null);
        }
        return null;
    }
}
