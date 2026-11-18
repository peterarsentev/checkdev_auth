package ru.checkdev.auth.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.dto.ProfileTgDTO;
import ru.checkdev.auth.service.PersonService;
import ru.checkdev.auth.service.RegistrationService;
import ru.checkdev.auth.domain.Role;
import ru.checkdev.auth.dto.CurrentUserResponse;
import ru.checkdev.auth.dto.RegistrationRequest;

import ru.checkdev.auth.dto.LoginRequest;
import ru.checkdev.auth.dto.TokenResponse;
import ru.checkdev.auth.service.LoginService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import java.util.Optional;

/**
 * @author parsentev
 * @since 26.09.2016
 */

@Tag(name = "AuthController", description = "Authentication REST API")
@RestController
public class AuthController {
    private final PersonService persons;
    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final String ping = "{}";

    @Autowired
    public AuthController(final PersonService persons, RegistrationService registrationService, LoginService loginService) {
        this.persons = persons;
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/login")
    @Operation(summary = "Войти по email и паролю и получить JWT")
    @ApiResponse(responseCode = "200", description = "JWT выдан на 15 минут")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(loginService.login(request));
    }

    @GetMapping("/user")
    @Operation(summary = "Проверить авторизацию и получить свой профиль",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Профиль владельца токена")
    @ApiResponse(responseCode = "401", description = "Токен отсутствует или недействителен")
    public CurrentUserResponse user(@AuthenticationPrincipal Jwt token) {
        var profile = persons.findByEmail(token.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new CurrentUserResponse(profile.getId(), profile.getUsername(), profile.getEmail(),
                profile.getRoles().stream().map(Role::getValue).toList());
    }

    @GetMapping("/ping")
    @Operation(summary = "Проверить доступность auth")
    public String ping() {
        return this.ping;
    }

    @GetMapping("/auth/activated/{key}")
    public Object activated(@PathVariable String key) {
        if (this.persons.activated(key)) {
            return new Object() {
                public boolean getSuccess() {
                    return true;
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "Notify has already activated";
                }
            };
        }
    }

    @PostMapping("/registration")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь создан и может войти")
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
    @ApiResponse(responseCode = "409", description = "Адрес электронной почты уже зарегистрирован")
    public ResponseEntity<ProfileTgDTO> registration(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/forgot")
    public Object forgot(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.forgot(profile);
        if (result.isPresent()) {
            return new Object() {
                public String getOk() {
                    return "ok";
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "E-mail не найден.";
                }
            };
        }
    }

    @PostMapping("/forgotTg")
    public Object forgotTg(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.forgotTg(profile);
        if (result.isPresent()) {
            return new Object() {
                public String getOk() {
                    return "ok";
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "E-mail не найден.";
                }
            };
        }
    }


}
