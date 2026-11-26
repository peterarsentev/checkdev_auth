package ru.checkdev.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.checkdev.auth.domain.Photo;
import ru.checkdev.auth.repository.PhotoRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PhotoServiceTest {
    @Mock
    private PhotoRepository photoRepository;
    @InjectMocks
    private PhotoService photoService;

    @Test
    void whenSavePhotoThenReturnPhotoWithId() {
        var random = ThreadLocalRandom.current();
        byte[] content = new byte[3];
        random.nextBytes(content);
        String name = UUID.randomUUID() + ".jpg";
        Photo photo = new Photo(content, name);
        Photo savedPhoto = new Photo(random.nextInt(1, Integer.MAX_VALUE), content.clone(), name);
        when(photoRepository.save(photo)).thenReturn(savedPhoto);
        assertEquals(savedPhoto, photoService.save(photo));
    }

    @Test
    void whenFindPhotoById() {
        var random = ThreadLocalRandom.current();
        int id = random.nextInt(1, Integer.MAX_VALUE);
        byte[] content = new byte[3];
        random.nextBytes(content);
        Photo photo = new Photo(id, content, UUID.randomUUID() + ".jpg");
        when(photoRepository.findById(id)).thenReturn(Optional.of(photo));
        assertEquals(photo, photoService.findImage(id));
    }

    @Test
    void whenNotFindPhotoById() {
        int id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        when(photoRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> photoService.findImage(id));
    }

}
