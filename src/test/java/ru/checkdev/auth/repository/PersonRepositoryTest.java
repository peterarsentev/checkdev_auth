package ru.checkdev.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import ru.checkdev.auth.dto.ProfileDTO;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * CheckDev пробное собеседование
 * Тест на класс PersonRepository
 *
 * @author Dmitry Stepanov
 * @version 22.09.2023'T'21:14
 */
@DataJpaTest()
public class PersonRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void injectedComponentAreNotNull() {
        assertNotNull(entityManager);
        assertNotNull(personRepository);
    }

    @Test
    public void whenFindProfileByIdThenReturnNull() {
        int id = -ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        ProfileDTO profileDTO = personRepository.findProfileById(id);
        assertNull(profileDTO);
    }

    @Test
    public void whenFindProfileOrderByCreatedDescThenReturnEmptyList() {
        entityManager.getEntityManager().createQuery("delete from profile").executeUpdate();
        var listProfileDTO = personRepository.findProfileOrderByCreatedDesc();
        assertThat(listProfileDTO).isEmpty();
    }
}
