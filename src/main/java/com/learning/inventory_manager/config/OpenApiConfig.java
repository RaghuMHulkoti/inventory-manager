package com.learning.inventory_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryManagerOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("https://supplier-service-tha0.onrender.com")
                                .description("Render Production Server")
                ));
    }
}
