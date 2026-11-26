package com.moviecatalog.movie_catalog.repository.integration;

import com.moviecatalog.movie_catalog.integration.support.AbstractPostgresIntegrationTest;
import com.moviecatalog.movie_catalog.dto.MovieActorDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.entity.MovieActor;
import com.moviecatalog.movie_catalog.enums.MovieStatus;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        showSql = false,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.liquibase.enabled=false"
        }
)
class MovieRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findActorsByMovieIdReturnsTheActorAndCharacterForThatMovie() {
        Actor actor = entityManager.persist(new Actor("Keanu", "Reeves", null, "USA"));

        Movie firstMovie = movie("Matrix");
        Movie secondMovie = movie("Matrix Reloaded");
        firstMovie.getActorList().add(movieActor(firstMovie, actor, "Neo"));
        secondMovie.getActorList().add(movieActor(secondMovie, actor, "Thomas Anderson"));

        entityManager.persist(firstMovie);
        entityManager.persist(secondMovie);
        entityManager.flush();

        Long firstMovieId = firstMovie.getId();
        Long secondMovieId = secondMovie.getId();
        Long actorId = actor.getId();

        assertThat(firstMovie.getActorList().getFirst().getId()).isNotNull();
        assertThat(secondMovie.getActorList().getFirst().getId()).isNotNull();

        entityManager.clear();

        Long persistedMovieActorCount = entityManager.getEntityManager()
                .createQuery("SELECT COUNT(movieActor) FROM MovieActor movieActor", Long.class)
                .getSingleResult();
        assertThat(persistedMovieActorCount).isEqualTo(2L);

        List<MovieActorDTO> firstMovieActors = movieRepository.findActorsByMovieId(firstMovieId);
        List<MovieActorDTO> secondMovieActors = movieRepository.findActorsByMovieId(secondMovieId);

        assertThat(firstMovieActors)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieActorDTO(firstMovieId, actorId, "Keanu", "Reeves", "Neo"));
        assertThat(secondMovieActors)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieActorDTO(secondMovieId, actorId, "Keanu", "Reeves", "Thomas Anderson"));
    }

    @Test
    void findActorsByMovieIdsReturnsActorsForAllRequestedMovies() {
        Actor firstActor = entityManager.persist(new Actor("Keanu", "Reeves", null, "USA"));
        Actor secondActor = entityManager.persist(new Actor("Laurence", "Fishburne", null, "USA"));
        Movie firstMovie = movie("Matrix");
        Movie secondMovie = movie("The Matrix Reloaded");
        firstMovie.getActorList().add(movieActor(firstMovie, firstActor, "Neo"));
        secondMovie.getActorList().add(movieActor(secondMovie, secondActor, "Morpheus"));

        entityManager.persist(firstMovie);
        entityManager.persist(secondMovie);
        entityManager.flush();
        entityManager.clear();

        Long firstMovieId = firstMovie.getId();
        Long secondMovieId = secondMovie.getId();

        List<MovieActorDTO> actual = movieRepository.findActorsByMovieIds(
                List.of(firstMovieId, secondMovieId)
        );

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new MovieActorDTO(firstMovie.getId(), firstActor.getId(), "Keanu", "Reeves", "Neo"),
                        new MovieActorDTO(secondMovie.getId(), secondActor.getId(), "Laurence", "Fishburne", "Morpheus")
                );
    }

    @Test
    void findDirectorsByMovieIdReturnsDirectorsForTheRequestedMovie() {
        Director director = entityManager.persist(director("Lana", "Wachowski"));
        Movie movie = movie("Matrix");
        movie.getDirectorList().add(director);

        entityManager.persist(movie);
        entityManager.flush();

        Long movieId = movie.getId();
        Long directorId = director.getId();

        entityManager.clear();

        List<MovieDirectorDTO> actual = movieRepository.findDirectorsByMovieId(movieId);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieDirectorDTO(movieId, directorId, "Lana", "Wachowski"));
    }

    @Test
    void findDirectorsByMovieIdsReturnsDirectorsForAllRequestedMovies() {
        Director firstDirector = entityManager.persist(director("Lana", "Wachowski"));
        Director secondDirector = entityManager.persist(director("Denis", "Villeneuve"));
        Movie firstMovie = movie("Matrix");
        Movie secondMovie = movie("Dune");
        firstMovie.getDirectorList().add(firstDirector);
        secondMovie.getDirectorList().add(secondDirector);
        entityManager.persist(firstMovie);
        entityManager.persist(secondMovie);
        entityManager.flush();

        Long firstMovieId = firstMovie.getId();
        Long secondMovieId = secondMovie.getId();
        Long firstDirectorId = firstDirector.getId();
        Long secondDirectorId = secondDirector.getId();

        entityManager.clear();

        List<MovieDirectorDTO> actual = movieRepository.findDirectorsByMovieIds(
                List.of(firstMovieId, secondMovieId)
        );

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new MovieDirectorDTO(firstMovieId, firstDirectorId, "Lana", "Wachowski"),
                        new MovieDirectorDTO(secondMovieId, secondDirectorId, "Denis", "Villeneuve")
                );
    }

    @Test
    void findMovieByIdReturnsMovieProjection() {
        Movie movie = entityManager.persistAndFlush(movie("Matrix"));
        entityManager.clear();

        MovieDTO actual = movieRepository.findMovieById(movie.getId()).orElseThrow();

        assertThat(actual.getId()).isEqualTo(movie.getId());
        assertThat(actual.getTitle()).isEqualTo("Matrix");
        assertThat(actual.getDescription()).isEqualTo("Test description");
        assertThat(actual.getReleaseYear()).isEqualTo(1999);
        assertThat(actual.getDurationMinutes()).isEqualTo(120);
        assertThat(actual.getAgeRating()).isEqualTo("PG-13");
        assertThat(actual.getStatus()).isEqualTo(MovieStatus.RELEASED);
        assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllMoviesReturnsMovieProjections() {
        Movie firstMovie = entityManager.persistAndFlush(movie("Matrix"));
        Movie secondMovie = entityManager.persistAndFlush(movie("Dune"));
        entityManager.clear();

        List<MovieDTO> actual = movieRepository.findAllMovies();

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting(MovieDTO::getId)
                .containsExactlyInAnyOrder(firstMovie.getId(), secondMovie.getId());
        assertThat(actual)
                .filteredOn(movieDTO -> movieDTO.getId().equals(firstMovie.getId()))
                .singleElement()
                .satisfies(movieDTO -> {
                    assertThat(movieDTO.getTitle()).isEqualTo("Matrix");
                    assertThat(movieDTO.getStatus()).isEqualTo(MovieStatus.RELEASED);
                    assertThat(movieDTO.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void associationQueriesReturnEmptyResultsWhenMovieHasNoActorsOrDirectors() {
        Movie movie = entityManager.persistAndFlush(movie("Standalone movie"));

        assertThat(movieRepository.findActorsByMovieId(movie.getId())).isEmpty();
        assertThat(movieRepository.findDirectorsByMovieId(movie.getId())).isEmpty();
    }

    @Test
    void bulkAssociationQueriesReturnEmptyResultsForEmptyMovieIds() {
        assertThat(movieRepository.findActorsByMovieIds(List.of())).isEmpty();
        assertThat(movieRepository.findDirectorsByMovieIds(List.of())).isEmpty();
    }

    @Test
    void removingMovieActorFromMovieCollectionDeletesLinkButKeepsActor() {
        Actor actor = entityManager.persist(new Actor("Keanu", "Reeves", null, "USA"));
        Movie movie = movie("Matrix");
        MovieActor movieActor = movieActor(movie, actor, "Neo");
        movie.getActorList().add(movieActor);
        entityManager.persistAndFlush(movie);

        Long movieId = movie.getId();
        Long movieActorId = movieActor.getId();
        Long actorId = actor.getId();
        entityManager.clear();

        Movie persistedMovie = movieRepository.findById(movieId).orElseThrow();
        persistedMovie.getActorList().clear();
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MovieActor.class, movieActorId)).isNull();
        assertThat(entityManager.find(Actor.class, actorId)).isNotNull();
    }

    private Movie movie(String title) {
        return new Movie(null, title, "Test description", 1999, 120, "PG-13", MovieStatus.RELEASED);
    }

    private MovieActor movieActor(Movie movie, Actor actor, String characterName) {
        return new MovieActor()
                .setMovie(movie)
                .setActor(actor)
                .setCharacterName(characterName);
    }

    private Director director(String firstName, String lastName) {
        return new Director(firstName, lastName, null, "Test country");
    }
}
