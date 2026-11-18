package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.ActorDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.mapper.ActorMapper;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
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
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @Spy
    private ActorMapper actorMapper = Mappers.getMapper(ActorMapper.class);

    @InjectMocks
    private ActorService actorService;

    @Test
    void shouldReturnCorrectActorById() {
        ActorDTO actorDTO = new ActorDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        ActorResponseDTO expectedResult = new ActorResponseDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.actorRepository.findActorById(1L)).thenReturn(Optional.of(actorDTO));

        var actualResult = actorService.getActorById(1L);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenActorNotFound() {
        when(actorRepository.findActorById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actorService.getActorById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldReturnCorrectAllActors() {
        ActorDTO actorDTO1 = new ActorDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        ActorDTO actorDTO2 = new ActorDTO(2L, "Keany", "Reevs", LocalDate.of(1925, 1, 1), "USA");
        List<ActorDTO> repositoryResponse = List.of(actorDTO1, actorDTO2);

        ActorResponseDTO actorResponseDTO1 = new ActorResponseDTO(1L,"Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        ActorResponseDTO actorResponseDTO2 = new ActorResponseDTO(2L, "Keany", "Reevs", LocalDate.of(1925, 1, 1), "USA");
        List<ActorResponseDTO> expectedResult = List.of(actorResponseDTO1, actorResponseDTO2);
        
        when(this.actorRepository.findAllActors()).thenReturn(repositoryResponse);

        var actualResult = actorService.getAllActors();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.size()).isEqualTo(2);
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulCreateActor() {
        ActorRequestDTO request = new ActorRequestDTO("Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        ActorResponseDTO expectedResult = new ActorResponseDTO(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
        
        when(this.actorRepository.save(any(Actor.class)))
                .thenReturn(new Actor(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA"));

        var actualResult = actorService.createActor(request);

        verify(actorRepository).save(actorCaptor.capture());

        Actor actorPassedToRepository = actorCaptor.getValue();

        assertThat(actorPassedToRepository.getFirstName()).isEqualTo("Tom");
        assertThat(actorPassedToRepository.getLastName()).isEqualTo("Cruise");
        assertThat(actorPassedToRepository.getBirthDate()).isEqualTo(LocalDate.of(1975, 11, 25));
        assertThat(actorPassedToRepository.getCountry()).isEqualTo("USA");
        
        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulUpdateActor() {
        ActorRequestDTO request = new ActorRequestDTO("Sam", "Cruise", LocalDate.of(1975, 11, 25), "USA");
        ActorResponseDTO expectedResult = new ActorResponseDTO(1L, "Sam", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.actorRepository.findById(1L)).thenReturn(Optional.of(new Actor(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA")));

        var actualResult = actorService.updateActor(1L, request);

        verify(actorRepository, never()).save(any(Actor.class));
        
        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentActor() {
        ActorRequestDTO request = new ActorRequestDTO("Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(actorRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> actorService.updateActor(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Actor not found with id: " + 1L);

        verify(actorRepository).findById(1L);
        verify(actorRepository, never()).save(any(Actor.class));
    }

    @Test
    void shouldSuccessfulDeleteActor() {
        Actor actor = new Actor(1L, "Tom", "Cruise", LocalDate.of(1975, 11, 25), "USA");

        when(this.actorRepository.findById(1L)).thenReturn(Optional.of(actor));

        actorService.deleteActor(1L);

        verify(actorRepository).delete(actor);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentActor() {
        when(actorRepository.findById(1L))
                .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> actorService.deleteActor(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Actor not found with id: " + 1L);

        verify(actorRepository).findById(1L);
        verify(actorRepository, never()).delete(any(Actor.class));
    }
}