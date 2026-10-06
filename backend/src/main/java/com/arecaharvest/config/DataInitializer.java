package com.arecaharvest.config;

import com.arecaharvest.entity.Role;
import com.arecaharvest.entity.User;
import com.arecaharvest.repository.RoleRepository;
import com.arecaharvest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds essential data for the demo (H2) profile.
 * Runs once at startup — idempotent.
 * For production (MySQL), Flyway V1 and V12 handle all seeding.
 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Demo profile — seeding essential data...");

        // ── Roles ──────────────────────────────────────────────────────────
        Role adminRole      = seedRole("ROLE_ADMIN",      "Head Harvester — full access");
        Role supervisorRole = seedRole("ROLE_SUPERVISOR", "Field Supervisor");
        Role workerRole     = seedRole("ROLE_WORKER",     "Harvesting Worker");

        // ── Users ──────────────────────────────────────────────────────────
        // admin / Password@123
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@arecaharvest.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .fullName("Rajesh Kumar (Admin)")
                    .phone("9876543210")
                    .active(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info("Created demo user: admin");
        }

        // supervisor1 / Password@123
        if (!userRepository.existsByUsername("supervisor1")) {
            User sup = User.builder()
                    .username("supervisor1")
                    .email("sup1@arecaharvest.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .fullName("Mohan Das (Supervisor)")
                    .phone("9876543211")
                    .active(true)
                    .roles(Set.of(supervisorRole))
                    .build();
            userRepository.save(sup);
            log.info("Created demo user: supervisor1");
        }

        // worker_raju / Password@123
        if (!userRepository.existsByUsername("worker_raju")) {
            User worker = User.builder()
                    .username("worker_raju")
                    .email("raju@arecaharvest.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .fullName("Raju Gowda (Worker)")
                    .phone("9876543212")
                    .active(true)
                    .roles(Set.of(workerRole))
                    .build();
            userRepository.save(worker);
            log.info("Created demo user: worker_raju");
        }

        log.info("Demo data seeding complete. Login: admin / Password@123");
    }

    private Role seedRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = Role.builder().name(name).description(description).build();
            return roleRepository.save(r);
        });
    }
}
