package ru.checkdev.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.domain.Role;
import ru.checkdev.auth.repository.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author parsentev
 * @since 21.09.2016
 */

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {
    @InjectMocks
    private PersonService service;

    @Mock
    private PersonRepository persons;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void whenRegDuplicatePersonThenResultEmpty() {
        Profile profile = randomProfile();
        profile.setPrivacy(true);
        when(persons.save(profile)).thenThrow(DataIntegrityViolationException.class);
        Optional<Profile> result = this.service.reg(profile);
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenRegPersonRolesThenDropRoles() {
        Profile profile = randomProfile();
        profile.setPrivacy(true);
        profile.setRoles(List.of(new Role("ROLE_" + UUID.randomUUID())));
        when(persons.save(profile)).thenReturn(profile);
        Profile savedProfile = this.service.reg(profile).orElseThrow();
        assertNull(savedProfile.getRoles());
    }

    @Test
    public void whenSelectAllPersonsThenListContainTestRecord() {
        Profile profile = randomProfile();
        profile.setId(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        List<Profile> expected = List.of(profile);
        when(persons.findAll()).thenReturn(expected);
        List<Profile> profileList = this.service.getAll();
        assertEquals(expected, profileList);
    }

    private static Profile randomProfile() {
        return new Profile("name-" + UUID.randomUUID(), UUID.randomUUID() + "@example.com",
                UUID.randomUUID().toString());
    }
}
