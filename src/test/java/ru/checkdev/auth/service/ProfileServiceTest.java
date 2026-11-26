package ru.checkdev.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.checkdev.auth.dto.ProfileDTO;
import ru.checkdev.auth.repository.PersonRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * CheckDev пробное собеседование
 * ProfileServiceTest тестирование слоя бизнес логики обработки модели ProfileDTO
 *
 * @author Dmitry Stepanov
 * @version 01:11
 */

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {
    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    public void whenFindByIDThenReturnOptionalProfileDTO() {
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        ProfileDTO expected = randomProfile(id);
        when(personRepository.findProfileById(id)).thenReturn(expected);
        var actual = profileService.findProfileByID(id);
        assertThat(actual.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void whenFindByIDThenReturnEmpty() {
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        when(personRepository.findProfileById(id)).thenReturn(null);
        var actual = profileService.findProfileByID(id);
        assertThat(actual).usingRecursiveComparison().isEqualTo(Optional.empty());
    }

    @Test
    public void whenFindProfilesOrderByCreatedDescThenReturnEmptyList() {
        when(personRepository.findProfileOrderByCreatedDesc()).thenReturn(Collections.emptyList());
        var actual = profileService.findProfilesOrderByCreatedDesc();
        assertThat(actual).usingRecursiveComparison().isEqualTo(Collections.emptyList());

    }

    @Test
    public void whenFindProfilesOrderByCreatedDescThenReturnListProfileDTO() {
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        var expected = List.of(randomProfile(id), randomProfile(id + 1));
        when(personRepository.findProfileOrderByCreatedDesc()).thenReturn(expected);
        var actual = profileService.findProfilesOrderByCreatedDesc();
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private static ProfileDTO randomProfile(int id) {
        return new ProfileDTO(id, "name-" + UUID.randomUUID(), "experience-" + UUID.randomUUID(),
                ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE), null, null);
    }
}
