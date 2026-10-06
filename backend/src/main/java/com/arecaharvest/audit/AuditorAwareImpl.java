package com.arecaharvest.audit;

import com.arecaharvest.config.security.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current authenticated username for JPA audit fields
 * (created_by, updated_by) on all BaseEntity subclasses.
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
                "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("SYSTEM");
        }
        if (auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return Optional.of(ud.getUsername());
        }
        return Optional.of(auth.getName());
    }
}
