package ru.checkdev.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.checkdev.auth.dto.ProfileDTO;
import ru.checkdev.auth.service.ProfileService;
import ru.checkdev.auth.web.controller.ProfileController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * CheckDev пробное собеседование
 * ProfileControllerTest тестирование RestController для отправки модели ProfileDTO.
 *
 * @author Dmitry Stepanov
 * @version 01:45
 */
public class ProfileControllerTest {
    @Test
    public void whenGetProfileByIdThenReturnStatusOK() throws Exception {
        ProfileService profileService = mock(ProfileService.class);
        MockMvc mockMvc = standaloneSetup(new ProfileController(profileService)).build();
        ProfileDTO profile = randomProfile(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        when(profileService.findProfileByID(profile.getId())).thenReturn(Optional.of(profile));
        mockMvc.perform(get("/profiles/{id}", profile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profile.getId()))
                .andExpect(jsonPath("$.username").value(profile.getUsername()))
                .andExpect(jsonPath("$.experience").value(profile.getExperience()))
                .andExpect(jsonPath("$.photoId").value(profile.getPhotoId()))
                .andDo(print());
    }

    @Test
    public void whenGetProfileByIdProfileNotFoundThenReturnStatusNoContent() throws Exception {
        ProfileService profileService = mock(ProfileService.class);
        MockMvc mockMvc = standaloneSetup(new ProfileController(profileService)).build();
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        when(profileService.findProfileByID(id)).thenReturn(Optional.empty());
        mockMvc.perform(get("/profiles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());
        verify(profileService).findProfileByID(id);
    }

    @Test
    public void whenGetAllProfilesOrderByCreateDescThenReturnStatusOkAndBody() throws Exception {
        ProfileService profileService = mock(ProfileService.class);
        MockMvc mockMvc = standaloneSetup(new ProfileController(profileService)).build();
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        var profiles = List.of(randomProfile(id), randomProfile(id + 1));
        when(profileService.findProfilesOrderByCreatedDesc()).thenReturn(profiles);
        mockMvc.perform(get("/profiles/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(profiles.size()))
                .andDo(print());
    }

    @Test
    public void whenGetAllProfilesOrderByCreateDescListEmptyThenReturnStatusNoContent() throws Exception {
        ProfileService profileService = mock(ProfileService.class);
        MockMvc mockMvc = standaloneSetup(new ProfileController(profileService)).build();
        when(profileService.findProfilesOrderByCreatedDesc()).thenReturn(List.of());
        mockMvc.perform(get("/profiles/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    private static ProfileDTO randomProfile(int id) {
        return new ProfileDTO(id, "name-" + UUID.randomUUID(), "experience-" + UUID.randomUUID(),
                ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE), null, null);
    }
}
