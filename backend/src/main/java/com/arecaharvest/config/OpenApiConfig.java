package com.arecaharvest.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT token obtained from /api/auth/login"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI arecaHarvestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArecaHarvest API")
                        .version("1.0.0")
                        .description("""
                                Integrated Areca Nut Harvesting Business Management Platform.
                                
                                Phase 1: Digitalization, workforce management, harvest tracking,
                                inventory, financial management, and business reporting.
                                
                                Phase 2 (future): Predictive analytics — yield prediction,
                                workforce optimization, intelligent scheduling.
                                """)
                        .contact(new Contact()
                                .name("ArecaHarvest Team")
                                .email("support@arecaharvest.com"))
                        .license(new License().name("Private — Research Use")));
    }
}
