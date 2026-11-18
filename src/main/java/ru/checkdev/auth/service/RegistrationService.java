package ru.checkdev.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.dto.ProfileTgDTO;
import ru.checkdev.auth.dto.RegistrationRequest;
import ru.checkdev.auth.repository.PersonRepository;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final PersonRepository persons;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ProfileTgDTO register(RegistrationRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Пароль должен занимать не более 72 байт UTF-8");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var profile = new Profile(request.username().trim(), email, passwordEncoder.encode(request.password()));
        profile.setPrivacy(true);
        profile.setActive(true);
        profile.setRoles(List.of());
        profile.setKey(UUID.randomUUID().toString());
        profile.setCreated(Calendar.getInstance());
        profile.setUpdated(Calendar.getInstance());
        var saved = persons.save(profile);
        return new ProfileTgDTO(saved.getId(), saved.getUsername(), saved.getEmail());
    }
}
