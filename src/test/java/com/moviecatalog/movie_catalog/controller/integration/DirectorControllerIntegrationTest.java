package com.moviecatalog.movie_catalog.controller.integration;

import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.integration.support.AbstractPostgresIntegrationTest;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
class DirectorControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DirectorRepository directorRepository;

    @BeforeEach
    void cleanDirectors() {
        directorRepository.deleteAll();
        directorRepository.flush();
    }

    @Test
    void createDirectorReturnsCreatedAndPersistsDirector() {
        DirectorRequestDTO request = new DirectorRequestDTO(
                "Greta", "Gerwig", LocalDate.of(1983, 8, 4), "USA"
        );

        ResponseEntity<DirectorResponseDTO> response = restTemplate.postForEntity(
                "/api/directors", request, DirectorResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        DirectorResponseDTO createdDirector = response.getBody();
        assertThat(createdDirector).isNotNull();
        assertDirectorResponse(createdDirector, createdDirector.getId(), "Greta", "Gerwig",
                LocalDate.of(1983, 8, 4), "USA");

        Director savedDirector = directorRepository.findById(createdDirector.getId()).orElseThrow();
        assertThat(savedDirector.getFirstName()).isEqualTo("Greta");
        assertThat(savedDirector.getLastName()).isEqualTo("Gerwig");
        assertThat(savedDirector.getBirthDate()).isEqualTo(LocalDate.of(1983, 8, 4));
        assertThat(savedDirector.getCountry()).isEqualTo("USA");

        assertDirectorResponse(response.getBody(), response.getBody().getId(), "Greta", "Gerwig", LocalDate.of(1983, 8, 4), "USA");
    }

    @Test
    void getAllDirectorsReturnsOkAndEmptyList() {
        ResponseEntity<List<DirectorResponseDTO>> response = restTemplate.exchange(
                "/api/directors", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void getAllDirectorsReturnsOkAndDirectorDetails() {
        Director firstDirector = createDirector("Director", "One", LocalDate.of(1975, 3, 4), "Canada");
        Director secondDirector = createDirector("Director", "Two", LocalDate.of(1985, 7, 8), "UK");

        ResponseEntity<List<DirectorResponseDTO>> response = restTemplate.exchange(
                "/api/directors", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<DirectorResponseDTO> directors = response.getBody();
        assertThat(directors).isNotNull().hasSize(2);
        assertThat(directors).extracting(DirectorResponseDTO::getId)
                .containsExactlyInAnyOrder(firstDirector.getId(), secondDirector.getId());

        DirectorResponseDTO firstResponse = directors.stream()
                .filter(director -> director.getId().equals(firstDirector.getId()))
                .findFirst()
                .orElseThrow();
        assertDirectorResponse(firstResponse, firstDirector.getId(), "Director", "One",
                LocalDate.of(1975, 3, 4), "Canada");

        DirectorResponseDTO secondResponse = directors.stream()
                .filter(director -> director.getId().equals(secondDirector.getId()))
                .findFirst()
                .orElseThrow();
        assertDirectorResponse(secondResponse, secondDirector.getId(), "Director", "Two",
                LocalDate.of(1985, 7, 8), "UK");
    }

    @Test
    void getDirectorByIdReturnsOkAndDirectorDetails() {
        Director director = createDirector("Denis", "Villeneuve", LocalDate.of(1967, 10, 3), "Canada");

        ResponseEntity<DirectorResponseDTO> response = restTemplate.getForEntity(
                "/api/directors/{id}", DirectorResponseDTO.class, director.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertDirectorResponse(response.getBody(), director.getId(), "Denis", "Villeneuve",
                LocalDate.of(1967, 10, 3), "Canada");
    }

    @Test
    void getDirectorByIdReturnsNotFoundWhenDirectorDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/directors/{id}", String.class, Long.MAX_VALUE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDirectorReturnsOkAndUpdatedDirectorDetails() {
        Director director = createDirector("Old", "Name", LocalDate.of(1980, 1, 1), "Old Country");
        DirectorRequestDTO update = new DirectorRequestDTO(
                "New", "Name", LocalDate.of(1990, 2, 3), "New Country"
        );

        ResponseEntity<DirectorResponseDTO> response = restTemplate.exchange(
                "/api/directors/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                DirectorResponseDTO.class, director.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertDirectorResponse(response.getBody(), director.getId(), "New", "Name",
                LocalDate.of(1990, 2, 3), "New Country");
    }

    @Test
    void updateDirectorReturnsNotFoundWhenDirectorDoesNotExist() {
        DirectorRequestDTO update = new DirectorRequestDTO(
                "New", "Name", LocalDate.of(1990, 2, 3), "New Country"
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/directors/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                String.class, Long.MAX_VALUE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteDirectorReturnsNoContentAndRemovesDirector() {
        Director director = createDirector("To", "Delete", LocalDate.of(1970, 5, 10), "Canada");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/directors/{id}", HttpMethod.DELETE, null, Void.class, director.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(directorRepository.existsById(director.getId())).isFalse();
    }

    @Test
    void deleteDirectorReturnsNotFoundWhenDirectorDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/directors/{id}", HttpMethod.DELETE, null, String.class, Long.MAX_VALUE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Director createDirector(String firstName, String lastName, LocalDate birthDate, String country) {
        return directorRepository.save(new Director(firstName, lastName, birthDate, country));
    }

    private void assertDirectorResponse(DirectorResponseDTO actual, Long id, String firstName,
                                       String lastName, LocalDate birthDate, String country) {
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getFirstName()).isEqualTo(firstName);
        assertThat(actual.getLastName()).isEqualTo(lastName);
        assertThat(actual.getBirthDate()).isEqualTo(birthDate);
        assertThat(actual.getCountry()).isEqualTo(country);
    }
}
