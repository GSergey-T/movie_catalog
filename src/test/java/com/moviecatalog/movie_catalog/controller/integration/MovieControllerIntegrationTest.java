package com.moviecatalog.movie_catalog.controller.integration;

import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieDirectorResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.dto.MovieActorDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.entity.MovieActor;
import com.moviecatalog.movie_catalog.enums.MovieStatus;
import com.moviecatalog.movie_catalog.integration.support.AbstractPostgresIntegrationTest;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestRestTemplate
class MovieControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @BeforeEach
    void cleanMoviesActorsAndDirectors() {
        movieRepository.deleteAll();
        directorRepository.deleteAll();
        actorRepository.deleteAll();
        movieRepository.flush();
        directorRepository.flush();
        actorRepository.flush();
    }

    @Test
    void createMovieWithoutActorsOrDirectorsReturnsCreatedWithEmptyLists() {
        MovieRequestDTO request = formingMovieRequest(List.of(), List.of());

        ResponseEntity<MovieResponseDTO> response = restTemplate.postForEntity(
                "/api/movies", request, MovieResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        MovieResponseDTO movie = response.getBody();
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isNotNull();
        assertMovieFields(movie);
        assertThat(movie.getDirectors()).isEmpty();
        assertThat(movie.getActors()).isEmpty();
        assertThat(movieRepository.existsById(movie.getId())).isTrue();
    }

    @Test
    void createMovieWithExistingActorsAndDirectorsReturnsCreatedWithAssociations() {
        Actor actor = actorRepository.save(new Actor(
                "Margot", "Robbie", LocalDate.of(1990, 7, 2), "Australia"
        ));
        Director director = directorRepository.save(new Director(
                "Greta", "Gerwig", LocalDate.of(1983, 8, 4), "USA"
        ));
        actorRepository.flush();
        directorRepository.flush();

        MovieRequestDTO request = formingMovieRequest(
                List.of(director.getId()),
                List.of(new MovieActorRequestDTO(actor.getId(), "Barbie"))
        );

        ResponseEntity<MovieResponseDTO> response = restTemplate.postForEntity(
                "/api/movies", request, MovieResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        MovieResponseDTO movie = response.getBody();
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isNotNull();
        assertMovieFields(movie);
        assertThat(movie.getDirectors()).hasSize(1);
        assertThat(movie.getDirectors().getFirst().getId()).isEqualTo(director.getId());
        assertThat(movie.getDirectors().getFirst().getFirstName()).isEqualTo("Greta");
        assertThat(movie.getDirectors().getFirst().getLastName()).isEqualTo("Gerwig");
        assertThat(movie.getActors()).hasSize(1);
        assertThat(movie.getActors().getFirst().getId()).isEqualTo(actor.getId());
        assertThat(movie.getActors().getFirst().getFirstName()).isEqualTo("Margot");
        assertThat(movie.getActors().getFirst().getLastName()).isEqualTo("Robbie");
        assertThat(movie.getActors().getFirst().getCharacterName()).isEqualTo("Barbie");
        assertThat(movieRepository.existsById(movie.getId())).isTrue();
    }

    @Test
    void createMovieWithUnknownActorReturnsNotFound() {
        MovieRequestDTO request = formingMovieRequest(
                List.of(),
                List.of(new MovieActorRequestDTO(Long.MAX_VALUE, "Unknown character"))
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/movies", request, String.class
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createMovieWithUnknownDirectorReturnsNotFound() {
        MovieRequestDTO request = formingMovieRequest(List.of(Long.MAX_VALUE), List.of());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/movies", request, String.class
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getMovieByIdWithoutAssociationsReturnsMovieWithEmptyLists() {
        Movie savedMovie = createMovieEntityWithoutAssociations("Solo movie");

        ResponseEntity<MovieResponseDTO> response = restTemplate.getForEntity(
                "/api/movies/{id}", MovieResponseDTO.class, savedMovie.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MovieResponseDTO movie = response.getBody();
        assertThat(movie).isNotNull();
        assertPersistedMovieFields(movie, savedMovie);
        assertThat(movie.getActors()).isEmpty();
        assertThat(movie.getDirectors()).isEmpty();
    }

    @Test
    void getMovieByIdWithAssociationsReturnsActorsAndDirectors() {
        SavedMovieWithAssociations saved = createMovieWithAssociations(
                "Movie with cast", "Actor", "One", "Director", "One", "Character One"
        );

        ResponseEntity<MovieResponseDTO> response = restTemplate.getForEntity(
                "/api/movies/{id}", MovieResponseDTO.class, saved.movie().getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MovieResponseDTO movie = response.getBody();
        assertThat(movie).isNotNull();
        assertPersistedMovieFields(movie, saved.movie());
        assertThat(movie.getActors()).hasSize(1);
        assertThat(movie.getDirectors()).hasSize(1);
        assertActorResponse(movie.getActors().getFirst(), saved.actor(), "Character One");
        assertDirectorResponse(movie.getDirectors().getFirst(), saved.director());
    }

    @Test
    void getMovieByIdReturnsNotFoundWhenMovieDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/movies/{id}", String.class, Long.MAX_VALUE
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllMoviesReturnsMoviesWithoutAssociations() {
        Movie firstMovie = createMovieEntityWithoutAssociations("First solo movie");
        Movie secondMovie = createMovieEntityWithoutAssociations("Second solo movie");

        ResponseEntity<List<MovieResponseDTO>> response = restTemplate.exchange(
                "/api/movies", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MovieResponseDTO> movies = response.getBody();
        assertThat(movies).isNotNull().hasSize(2);
        MovieResponseDTO firstResponse = movies.stream()
                .filter(movie -> movie.getId().equals(firstMovie.getId()))
                .findFirst().orElseThrow();
        assertPersistedMovieFields(firstResponse, firstMovie);
        assertThat(firstResponse.getActors()).isEmpty();
        assertThat(firstResponse.getDirectors()).isEmpty();

        MovieResponseDTO secondResponse = movies.stream()
                .filter(movie -> movie.getId().equals(secondMovie.getId()))
                .findFirst().orElseThrow();
        assertPersistedMovieFields(secondResponse, secondMovie);
        assertThat(secondResponse.getActors()).isEmpty();
        assertThat(secondResponse.getDirectors()).isEmpty();
    }

    @Test
    void getAllMoviesReturnsAssociationsGroupedByMovie() {
        SavedMovieWithAssociations first = createMovieWithAssociations(
                "First linked movie", "Actor", "One", "Director", "One", "Character One"
        );
        SavedMovieWithAssociations second = createMovieWithAssociations(
                "Second linked movie", "Actor", "Two", "Director", "Two", "Character Two"
        );

        ResponseEntity<List<MovieResponseDTO>> response = restTemplate.exchange(
                "/api/movies", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MovieResponseDTO> movies = response.getBody();
        assertThat(movies).isNotNull().hasSize(2);

        MovieResponseDTO firstResponse = movies.stream()
                .filter(movie -> movie.getId().equals(first.movie().getId()))
                .findFirst().orElseThrow();
        assertPersistedMovieFields(firstResponse, first.movie());
        assertThat(firstResponse.getActors()).hasSize(1);
        assertActorResponse(firstResponse.getActors().getFirst(), first.actor(), "Character One");
        assertThat(firstResponse.getDirectors()).hasSize(1);
        assertDirectorResponse(firstResponse.getDirectors().getFirst(), first.director());

        MovieResponseDTO secondResponse = movies.stream()
                .filter(movie -> movie.getId().equals(second.movie().getId()))
                .findFirst().orElseThrow();
        assertPersistedMovieFields(secondResponse, second.movie());
        assertThat(secondResponse.getActors()).hasSize(1);
        assertActorResponse(secondResponse.getActors().getFirst(), second.actor(), "Character Two");
        assertThat(secondResponse.getDirectors()).hasSize(1);
        assertDirectorResponse(secondResponse.getDirectors().getFirst(), second.director());
    }

    @Test
    void getAllMoviesReturnsEmptyListWhenNoMoviesExist() {
        ResponseEntity<List<MovieResponseDTO>> response = restTemplate.exchange(
                "/api/movies", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void updateMovieWithoutAssociationsReturnsUpdatedMovieWithEmptyLists() {
        Movie movie = createMovieEntityWithoutAssociations("Before update");
        MovieRequestDTO update = updatedMovieRequest(List.of(), List.of());

        ResponseEntity<MovieResponseDTO> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                MovieResponseDTO.class, movie.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MovieResponseDTO updatedMovie = response.getBody();
        assertThat(updatedMovie).isNotNull();
        assertUpdatedMovieFields(updatedMovie);
        assertThat(updatedMovie.getActors()).isEmpty();
        assertThat(updatedMovie.getDirectors()).isEmpty();
        assertThat(movieRepository.findById(movie.getId()).orElseThrow().getTitle())
                .isEqualTo("Updated movie");
    }

    @Test
    void updateMovieWithEmptyAssociationsClearsExistingAssociations() {
        SavedMovieWithAssociations oldLinks = createMovieWithAssociations(
                "Before update", "OldActor", "Old", "OldDirector", "Old", "Old character"
        );
        MovieRequestDTO update = updatedMovieRequest(
                List.of(),
                List.of()
        );
        
        ResponseEntity<MovieResponseDTO> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                MovieResponseDTO.class, oldLinks.movie.getId()
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MovieResponseDTO updatedMovie = response.getBody();
        assertThat(updatedMovie).isNotNull();
        assertUpdatedMovieFields(updatedMovie);
        assertThat(updatedMovie.getActors()).isEmpty();
        assertThat(updatedMovie.getDirectors()).isEmpty();
        assertThat(movieRepository.findActorsByMovieId(oldLinks.movie().getId()))
                .isEmpty();
        assertThat(movieRepository.findDirectorsByMovieId(oldLinks.movie().getId()))
                .isEmpty();
        assertThat(actorRepository.existsById(oldLinks.actor().getId())).isTrue();
        assertThat(directorRepository.existsById(oldLinks.director().getId())).isTrue();
    }

    @Test
    void updateMovieWithAssociationsReplacesActorsAndDirectors() {
        SavedMovieWithAssociations oldLinks = createMovieWithAssociations(
                "Before update", "OldActor", "Old", "OldDirector", "Old", "Old character"
        );
        Actor newActor = actorRepository.saveAndFlush(new Actor(
                "NewActor", "New", LocalDate.of(1980, 1, 1), "New country"
        ));
        Director newDirector = directorRepository.saveAndFlush(new Director(
                "NewDirector", "New", LocalDate.of(1981, 1, 1), "New country"
        ));
        MovieRequestDTO update = updatedMovieRequest(
                List.of(newDirector.getId()),
                List.of(new MovieActorRequestDTO(newActor.getId(), "New character"))
        );

        ResponseEntity<MovieResponseDTO> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.PUT, new HttpEntity<>(update),
                MovieResponseDTO.class, oldLinks.movie().getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MovieResponseDTO updatedMovie = response.getBody();
        assertThat(updatedMovie).isNotNull();
        assertUpdatedMovieFields(updatedMovie);
        assertThat(updatedMovie.getActors()).hasSize(1);
        assertActorResponse(updatedMovie.getActors().getFirst(), newActor, "New character");
        assertThat(updatedMovie.getDirectors()).hasSize(1);
        assertDirectorResponse(updatedMovie.getDirectors().getFirst(), newDirector);

        List<MovieActorDTO> persistedActors = movieRepository.findActorsByMovieId(oldLinks.movie().getId());
        assertThat(persistedActors).hasSize(1);
        assertThat(persistedActors.getFirst().getActorId()).isEqualTo(newActor.getId());
        assertThat(persistedActors.getFirst().getCharacterName()).isEqualTo("New character");
        List<MovieDirectorDTO> persistedDirectors = movieRepository.findDirectorsByMovieId(oldLinks.movie().getId());
        assertThat(persistedDirectors).hasSize(1);
        assertThat(persistedDirectors.getFirst().getDirectorId()).isEqualTo(newDirector.getId());
        assertThat(actorRepository.existsById(oldLinks.actor().getId())).isTrue();
        assertThat(directorRepository.existsById(oldLinks.director().getId())).isTrue();
    }

    @Test
    void updateMovieReturnsNotFoundWhenMovieDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.PUT,
                new HttpEntity<>(updatedMovieRequest(List.of(), List.of())),
                String.class, Long.MAX_VALUE
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteMovieWithoutAssociationsReturnsNoContentAndRemovesMovie() {
        Movie movie = createMovieEntityWithoutAssociations("Movie to delete");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.DELETE, null, Void.class, movie.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(movieRepository.existsById(movie.getId())).isFalse();
    }

    @Test
    void deleteMovieWithAssociationsRemovesMovieAndKeepsActorsAndDirectors() {
        SavedMovieWithAssociations saved = createMovieWithAssociations(
                "Linked movie to delete", "Actor", "One", "Director", "One", "Character One"
        );
        ResponseEntity<MovieResponseDTO> beforeDelete = restTemplate.getForEntity(
                "/api/movies/{id}", MovieResponseDTO.class, saved.movie().getId()
        );
        assertThat(beforeDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(beforeDelete.getBody().getActors()).hasSize(1);
        assertThat(beforeDelete.getBody().getDirectors()).hasSize(1);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.DELETE, null, Void.class, saved.movie().getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(movieRepository.existsById(saved.movie().getId())).isFalse();
        assertThat(actorRepository.existsById(saved.actor().getId())).isTrue();
        assertThat(directorRepository.existsById(saved.director().getId())).isTrue();
        assertThat(movieRepository.findActorsByMovieId(saved.movie().getId())).isEmpty();
        assertThat(movieRepository.findDirectorsByMovieId(saved.movie().getId())).isEmpty();
    }

    @Test
    void deleteMovieReturnsNotFoundWhenMovieDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/movies/{id}", HttpMethod.DELETE, null, String.class, Long.MAX_VALUE
        );

        assertThat(movieRepository.count()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private MovieRequestDTO formingMovieRequest(List<Long> directorIds, List<MovieActorRequestDTO> actors) {
        return new MovieRequestDTO(
                "Barbie", "A test movie", 2023, 114, "PG-13", MovieStatus.RELEASED,
                directorIds, actors
        );
    }

    private void assertMovieFields(MovieResponseDTO movie) {
        assertThat(movie.getTitle()).isEqualTo("Barbie");
        assertThat(movie.getDescription()).isEqualTo("A test movie");
        assertThat(movie.getReleaseYear()).isEqualTo(2023);
        assertThat(movie.getDurationMinutes()).isEqualTo(114);
        assertThat(movie.getAgeRating()).isEqualTo("PG-13");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.RELEASED);
        assertThat(movie.getCreatedAt()).isNotNull();
    }

    private Movie createMovieEntityWithoutAssociations(String title) {
        return movieRepository.saveAndFlush(new Movie(
                null, title, "Persisted test movie", 2020, 100, "PG", MovieStatus.RELEASED
        ));
    }

    private SavedMovieWithAssociations createMovieWithAssociations(String title,
                                                                   String actorFirstName,
                                                                   String actorLastName,
                                                                   String directorFirstName,
                                                                   String directorLastName,
                                                                   String characterName) {
        Actor actor = actorRepository.saveAndFlush(new Actor(
                actorFirstName, actorLastName, LocalDate.of(1970, 1, 2), "Test actor country"
        ));
        Director director = directorRepository.saveAndFlush(new Director(
                directorFirstName, directorLastName, LocalDate.of(1971, 2, 3), "Test director country"
        ));
        Movie movie = new Movie(null, title, "Persisted test movie", 2020, 100, "PG", MovieStatus.RELEASED);
        movie.setDirectorList(new ArrayList<>(List.of(director)));
        MovieActor movieActor = new MovieActor()
                .setMovie(movie)
                .setActor(actor)
                .setCharacterName(characterName);
        movie.setActorList(new ArrayList<>(List.of(movieActor)));
        movie = movieRepository.saveAndFlush(movie);
        return new SavedMovieWithAssociations(movie, actor, director);
    }

    private MovieRequestDTO updatedMovieRequest(List<Long> directorIds, List<MovieActorRequestDTO> actors) {
        return new MovieRequestDTO(
                "Updated movie", "Updated description", 2024, 125, "PG-13",
                MovieStatus.RELEASED, directorIds, actors
        );
    }

    private void assertPersistedMovieFields(MovieResponseDTO actual, Movie expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
        assertThat(actual.getReleaseYear()).isEqualTo(expected.getReleaseYear());
        assertThat(actual.getDurationMinutes()).isEqualTo(expected.getDurationMinutes());
        assertThat(actual.getAgeRating()).isEqualTo(expected.getAgeRating());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getCreatedAt()).isNotNull();
    }

    private void assertUpdatedMovieFields(MovieResponseDTO movie) {
        assertThat(movie.getTitle()).isEqualTo("Updated movie");
        assertThat(movie.getDescription()).isEqualTo("Updated description");
        assertThat(movie.getReleaseYear()).isEqualTo(2024);
        assertThat(movie.getDurationMinutes()).isEqualTo(125);
        assertThat(movie.getAgeRating()).isEqualTo("PG-13");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.RELEASED);
        assertThat(movie.getCreatedAt()).isNotNull();
    }

    private void assertActorResponse(MovieActorResponseDTO actual, Actor expected, String characterName) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        assertThat(actual.getCharacterName()).isEqualTo(characterName);
    }

    private void assertDirectorResponse(MovieDirectorResponseDTO actual, Director expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
    }

    private record SavedMovieWithAssociations(Movie movie, Actor actor, Director director) {
    }
}
