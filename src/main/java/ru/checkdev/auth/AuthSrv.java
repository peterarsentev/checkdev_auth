package ru.checkdev.auth;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@OpenAPIDefinition(info = @Info(title = "Auth - authentication service", description = "Description"))
@SpringBootApplication
@EnableEurekaClient
public class AuthSrv {

    public static void main(String[] args) {
        SpringApplication.run(AuthSrv.class, args);
    }

}
