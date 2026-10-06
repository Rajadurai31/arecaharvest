package com.arecaharvest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ArecaHarvest — Integrated Areca Nut Harvesting Business Management Platform
 *
 * Phase 1: Digitalization, operational management, and structured data collection.
 * Phase 2 (future): Predictive analytics and intelligent scheduling.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableAsync
@EnableScheduling
public class ArecaHarvestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArecaHarvestApplication.class, args);
    }
}
