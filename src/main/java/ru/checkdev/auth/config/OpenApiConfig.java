package ru.checkdev.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI authApi() {
        return new OpenAPI()
                .info(new Info().title("CheckDev Auth").version("1.0")
                        .description("Регистрация, вход по JSON и авторизация по JWT. "
                                + "Получите access_token через POST /login и вставьте его в Authorize."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
