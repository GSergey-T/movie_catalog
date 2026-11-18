package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.DirectorDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.mapper.DirectorMapper;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {
    
    @Mock
    private DirectorRepository directorRepository;
    
    @Spy
    private DirectorMapper directorMapper = Mappers.getMapper(DirectorMapper.class);
    
    @InjectMocks
    private DirectorService directorService;

    @Test
    void shouldReturnCorrectDirectorById() {
        DirectorDTO directorDTO = new DirectorDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        DirectorResponseDTO expectedResult = new DirectorResponseDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.directorRepository.findDirectorById(1L)).thenReturn(Optional.of(directorDTO));

        var actualResult = directorService.getDirectorById(1L);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenDirectorNotFound() {
        when(directorRepository.findDirectorById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.getDirectorById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldReturnCorrectAllDirectors() {
        DirectorDTO directorDTO1 = new DirectorDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        DirectorDTO directorDTO2 = new DirectorDTO(2L, "Keany", "Reevs", LocalDate.of(1925, 1, 1), "USA");
        List<DirectorDTO> repositoryResponse = List.of(directorDTO1, directorDTO2);

        DirectorResponseDTO directorResponseDTO1 = new DirectorResponseDTO(1L,"Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        DirectorResponseDTO directorResponseDTO2 = new DirectorResponseDTO(2L, "Keany", "Reevs", LocalDate.of(1925, 1, 1), "USA");
        List<DirectorResponseDTO> expectedResult = List.of(directorResponseDTO1, directorResponseDTO2);

        when(this.directorRepository.findAllDirectors()).thenReturn(repositoryResponse);

        var actualResult = directorService.getAllDirectors();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.size()).isEqualTo(2);
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulCreateDirector() {
        DirectorRequestDTO request = new DirectorRequestDTO("Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        DirectorResponseDTO expectedResult = new DirectorResponseDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        ArgumentCaptor<Director> directorCaptor = ArgumentCaptor.forClass(Director.class);

        when(this.directorRepository.save(any(Director.class)))
                .thenReturn(new Director(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA"));

        var actualResult = directorService.createDirector(request);

        verify(directorRepository).save(directorCaptor.capture());

        Director directorPassedToRepository = directorCaptor.getValue();

        assertThat(directorPassedToRepository.getFirstName()).isEqualTo("Tom");
        assertThat(directorPassedToRepository.getLastName()).isEqualTo("Cruise");
        assertThat(directorPassedToRepository.getBirthDate()).isEqualTo(LocalDate.of(1975, 11, 25));
        assertThat(directorPassedToRepository.getCountry()).isEqualTo("USA");

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulUpdateDirector() {
        DirectorRequestDTO request = new DirectorRequestDTO("Sam", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        DirectorResponseDTO expectedResult = new DirectorResponseDTO(1L, "Sam", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.directorRepository.findById(1L))
                .thenReturn(Optional.of(new Director(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA")));

        var actualResult = directorService.updateDirector(1L, request);

        verify(directorRepository, never()).save(any(Director.class));

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDirector() {
        DirectorRequestDTO request = new DirectorRequestDTO("Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(directorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.updateDirector(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Director not found with id: " + 1L);

        verify(directorRepository).findById(1L);
        verify(directorRepository, never()).save(any(Director.class));
    }

    @Test
    void shouldSuccessfulDeleteDirector() {
        Director director = new Director(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.directorRepository.findById(1L))
                .thenReturn(Optional.of(director));

        directorService.deleteDirector(1L);

        verify(directorRepository).delete(director);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDirector() {
        when(directorRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.deleteDirector(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Director not found with id: " + 1L);

        verify(directorRepository).findById(1L);
        verify(directorRepository, never()).delete(any(Director.class));
    }
}