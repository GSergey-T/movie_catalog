package com.moviecatalog.movie_catalog.controller.integration;

import com.moviecatalog.movie_catalog.integration.support.AbstractPostgresIntegrationTest;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestRestTemplate  
class ActorControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActorRepository actorRepository;

    @BeforeEach
    void cleanActors() {
        actorRepository.deleteAll();
        actorRepository.flush();
    }

    @Test
    void getAllActorsReturnsOkAndEmptyList() {
        ResponseEntity<List<ActorResponseDTO>> response = restTemplate.exchange(
                "/api/actors",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void createActorReturnsCreatedAndPersistsActor() {
        ActorRequestDTO request = new ActorRequestDTO(
                "Viola", "Davis", LocalDate.of(1965, 8, 11), "USA"
        );

        ResponseEntity<ActorResponseDTO> response = restTemplate.postForEntity(
                "/api/actors", request, ActorResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ActorResponseDTO createdActor = response.getBody();
        assertThat(createdActor).isNotNull();
        assertActorResponse(createdActor, createdActor.getId(), "Viola", "Davis", LocalDate.of(1965, 8, 11), "USA");

        Actor savedActor = actorRepository.findById(createdActor.getId()).orElseThrow();
        assertThat(savedActor.getFirstName()).isEqualTo("Viola");
        assertThat(savedActor.getLastName()).isEqualTo("Davis");
        assertThat(savedActor.getBirthDate()).isEqualTo(LocalDate.of(1965, 8, 11));
        assertThat(savedActor.getCountry()).isEqualTo("USA");
    }

    @Test
    void getAllActorsReturnsOkAndActorDetails() {
        Actor firstActor = createActor("Actor", "One", LocalDate.of(1975, 3, 4), "Canada");
        Actor secondActor = createActor("Actor", "Two", LocalDate.of(1985, 7, 8), "UK");

        ResponseEntity<List<ActorResponseDTO>> response = restTemplate.exchange(
                "/api/actors",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ActorResponseDTO> actors = response.getBody();
        assertThat(actors).isNotNull().hasSize(2);
        assertThat(actors).extracting(ActorResponseDTO::getId)
                .containsExactlyInAnyOrder(firstActor.getId(), secondActor.getId());
        assertThat(actors).extracting(actor -> actor.getFirstName() + " " + actor.getLastName())
                .containsExactlyInAnyOrder("Actor One", "Actor Two");

        ActorResponseDTO firstResponse = actors.stream()
                .filter(actor -> actor.getId().equals(firstActor.getId()))
                .findFirst()
                .orElseThrow();
        assertActorResponse(firstResponse, firstActor.getId(), "Actor", "One",
                LocalDate.of(1975, 3, 4), "Canada");

        ActorResponseDTO secondResponse = actors.stream()
                .filter(actor -> actor.getId().equals(secondActor.getId()))
                .findFirst()
                .orElseThrow();
        assertActorResponse(secondResponse, secondActor.getId(), "Actor", "Two",
                LocalDate.of(1985, 7, 8), "UK");
    }

    @Test
    void getActorByIdReturnsOkAndActorDetails() {
        Actor actor = createActor("Meryl", "Streep", LocalDate.of(1949, 6, 22), "USA");

        ResponseEntity<ActorResponseDTO> response = restTemplate.getForEntity(
                "/api/actors/{id}", ActorResponseDTO.class, actor.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertActorResponse(response.getBody(), actor.getId(), "Meryl", "Streep",
                LocalDate.of(1949, 6, 22), "USA");
    }

    @Test
    void getActorByIdReturnsNotFoundWhenActorDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/actors/{id}", String.class, Long.MAX_VALUE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateActorReturnsOkAndUpdatedActorDetails() {
        Actor actor = createActor("Old", "Name", LocalDate.of(1980, 1, 1), "Old Country");
        ActorRequestDTO update = new ActorRequestDTO("New", "Name", LocalDate.of(1990, 2, 3), "New Country");

        ResponseEntity<ActorResponseDTO> response = restTemplate.exchange(
                "/api/actors/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(update),
                ActorResponseDTO.class,
                actor.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertActorResponse(response.getBody(), actor.getId(), "New", "Name", LocalDate.of(1990, 2, 3), "New Country");
    }

    @Test
    void updateActorReturnsNotFoundWhenActorDoesNotExist() {
        ActorRequestDTO update = new ActorRequestDTO("New", "Name", LocalDate.of(1990, 2, 3), "New Country");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/actors/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                String.class, Long.MAX_VALUE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteActorReturnsNoContentAndRemovesActor() {
        Actor actor = createActor("To", "Delete", LocalDate.of(1970, 5, 10), "Canada");

        ResponseEntity<Void> response = restTemplate.exchange("/api/actors/{id}", HttpMethod.DELETE, null, Void.class, actor.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(actorRepository.existsById(actor.getId())).isFalse();
    }

    @Test
    void deleteActorReturnsNotFoundWhenActorDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange("/api/actors/{id}", HttpMethod.DELETE, null, String.class, Long.MAX_VALUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Actor createActor(String firstName, String lastName, LocalDate birthDate, String country) {
        return actorRepository.save(new Actor(firstName, lastName, birthDate, country));
    }

    private void assertActorResponse(ActorResponseDTO actual, Long id, String firstName, String lastName, LocalDate birthDate, String country) {
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getFirstName()).isEqualTo(firstName);
        assertThat(actual.getLastName()).isEqualTo(lastName);
        assertThat(actual.getBirthDate()).isEqualTo(birthDate);
        assertThat(actual.getCountry()).isEqualTo(country);
    }
}
