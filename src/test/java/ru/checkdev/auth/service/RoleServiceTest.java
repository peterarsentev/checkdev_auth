package ru.checkdev.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.checkdev.auth.domain.Role;
import ru.checkdev.auth.repository.RoleRepository;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


/**
 * @author parsentev
 * @since 21.09.2016
 */
@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @InjectMocks
    private RoleService service;
    @Mock
    private RoleRepository roles;

    @Test
    public void whenAddRolesThenPersonHasRoles() {
        String name = "ROLE_" + UUID.randomUUID();
        Role role = new Role(name);
        Role savedRole = new Role(name);
        savedRole.setId(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        when(roles.save(role)).thenReturn(savedRole);
        Role result = this.service.save(role);
        assertThat(result).usingRecursiveComparison().isEqualTo(savedRole);
    }
}
