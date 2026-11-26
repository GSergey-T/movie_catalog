package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.dto.MovieActorDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.*;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.entity.MovieActor;
import com.moviecatalog.movie_catalog.enums.MovieStatus;
import com.moviecatalog.movie_catalog.mapper.DirectorMapper;
import com.moviecatalog.movie_catalog.mapper.MovieMapper;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private ActorRepository actorRepository;

    @Spy
    private DirectorMapper directorMapper = Mappers.getMapper(DirectorMapper.class);

    @Spy
    @InjectMocks
    private MovieMapper movieMapper = Mappers.getMapper(MovieMapper.class);

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldReturnMovieByIdWithDirectorsAndActors() {
        LocalDateTime createdDate = LocalDateTime.now();

        MovieResponseDTO expectedResult = formingMovieResponseDTO(1L, "Matrix", 1999, createdDate,
                List.of(new MovieDirectorResponseDTO(4L, "Lana", "Wachowski")),
                List.of(new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo")));

        when(movieRepository.findMovieById(1L))
                .thenReturn(Optional.of(new MovieDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate)));
        when(movieRepository.findDirectorsByMovieId(1L))
                .thenReturn(List.of(new MovieDirectorDTO(1L, 4L, "Lana", "Wachowski")));
        when(movieRepository.findActorsByMovieId(1L))
                .thenReturn(List.of(new MovieActorDTO(1L, 6L, "Keanu", "Reeves", "Neo")));

        var actualResult = movieService.getMovieById(1L);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
        verify(movieRepository).findMovieById(1L);
        verify(movieRepository).findDirectorsByMovieId(1L);
        verify(movieRepository).findActorsByMovieId(1L);
    }

    @Test
    void shouldReturnEmptyAssociationListsWhenMovieHasNoDirectorsOrActors() {
        LocalDateTime createdDate = LocalDateTime.now();
        MovieResponseDTO expectedResult = formingMovieResponseDTO(1L, "Matrix", 1999, createdDate, Collections.emptyList(), Collections.emptyList());

        when(movieRepository.findMovieById(1L))
                .thenReturn(Optional.of(new MovieDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate)));
        when(movieRepository.findDirectorsByMovieId(1L))
                .thenReturn(List.of());
        when(movieRepository.findActorsByMovieId(1L))
                .thenReturn(List.of());

        MovieResponseDTO actualResult = movieService.getMovieById(1L);

        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
        assertThat(actualResult.getDirectors()).isEmpty();
        assertThat(actualResult.getActors()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        when(movieRepository.findMovieById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Movie not found with id: " + 1L);

        verify(movieRepository).findMovieById(1L);
        verify(movieRepository, never()).findDirectorsByMovieId(anyLong());
        verify(movieRepository, never()).findActorsByMovieId(anyLong());
    }

    @Test
    void shouldReturnAllMoviesWithDirectorsAndActorsGroupedByMovie() {
        LocalDateTime createdDate = LocalDateTime.now();

        MovieDTO movieDTO1 = new MovieDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate);
        MovieDTO movieDTO2 = new MovieDTO(2L, "Matrix-2", "AI", 2003, 132, "18+", MovieStatus.RELEASED, createdDate);
        List<MovieDTO> repositoryResponse = List.of(movieDTO1, movieDTO2);
        MovieResponseDTO movieResponseDTO1 = formingMovieResponseDTO(1L, "Matrix", 1999, createdDate,
                List.of(new MovieDirectorResponseDTO(4L, "Lana", "Wachowski")),
                List.of(new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo")));
        MovieResponseDTO movieResponseDTO2 = formingMovieResponseDTO(2L, "Matrix-2", 2003, createdDate,
                List.of(new MovieDirectorResponseDTO(5L, "Denis", "Villeneuve")),
                List.of(new MovieActorResponseDTO(7L, "Laurence", "Fishburne", "Morpheus")));
        List<MovieResponseDTO> expectedResult = List.of(movieResponseDTO1, movieResponseDTO2);

        when(movieRepository.findAllMovies())
                .thenReturn(repositoryResponse);
        when(movieRepository.findDirectorsByMovieIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new MovieDirectorDTO(1L, 4L, "Lana", "Wachowski"),
                        new MovieDirectorDTO(2L, 5L, "Denis", "Villeneuve")
                ));
        when(movieRepository.findActorsByMovieIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new MovieActorDTO(1L, 6L, "Keanu", "Reeves", "Neo"),
                        new MovieActorDTO(2L, 7L, "Laurence", "Fishburne", "Morpheus")
                ));

        var actualResult = movieService.getAllMovies();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.size()).isEqualTo(2);
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
        verify(movieRepository).findDirectorsByMovieIds(List.of(1L, 2L));
        verify(movieRepository).findActorsByMovieIds(List.of(1L, 2L));
        verify(movieRepository, never()).findDirectorsByMovieId(anyLong());
        verify(movieRepository, never()).findActorsByMovieId(anyLong());
    }

    @Test
    void shouldReturnEmptyAssociationListsForMovieWithoutActorsAndDirectors() {
        LocalDateTime createdDate = LocalDateTime.now();

        MovieDTO firstMovie = new MovieDTO(
                1L, "Matrix", "AI", 1999, 132, "18+",
                MovieStatus.RELEASED, createdDate
        );
        MovieDTO secondMovie = new MovieDTO(
                2L, "Interstellar", "Space", 2014, 169, "12+",
                MovieStatus.RELEASED, createdDate
        );

        when(movieRepository.findAllMovies())
                .thenReturn(List.of(firstMovie, secondMovie));

        when(movieRepository.findDirectorsByMovieIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new MovieDirectorDTO(1L, 4L, "Lana", "Wachowski")
                ));

        when(movieRepository.findActorsByMovieIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        new MovieActorDTO(1L, 6L, "Keanu", "Reeves", "Neo")
                ));

        List<MovieResponseDTO> result = movieService.getAllMovies();

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getActors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo"));
        assertThat(result.get(0).getDirectors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieDirectorResponseDTO(4L, "Lana", "Wachowski"));

        assertThat(result.get(1).getActors()).isEmpty();
        assertThat(result.get(1).getDirectors()).isEmpty();
    }

    @Test
    void shouldNotQueryDirectorsAndActorsWhenNoMoviesAreFound() {
        when(movieRepository.findAllMovies()).thenReturn(List.of());

        assertThat(movieService.getAllMovies()).isEmpty();

        verify(movieRepository, never()).findDirectorsByMovieIds(anyList());
        verify(movieRepository, never()).findActorsByMovieIds(anyList());
    }

    @Test
    void shouldThrowExceptionWhenCreatingMovieWithOneUnknownActorAmongMultipleActors() {
        Actor existingActor = formingActorEntity(6L, "Keanu", "Reeves");

        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of())
                .setActors(List.of(
                        new MovieActorRequestDTO(6L, "Neo"),
                        new MovieActorRequestDTO(404L, "Unknown")
                ));

        when(actorRepository.findAllById(List.of(6L, 404L)))
                .thenReturn(List.of(existingActor));

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");

        verify(actorRepository).findAllById(List.of(6L, 404L));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldCreateMovieWithoutDirectorsOrActors() {
        LocalDateTime createdDate = LocalDateTime.now();
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of());
        MovieResponseDTO expectedResult = formingMovieResponseDTO(1L, "Matrix", 1999, createdDate, List.of(), List.of());

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(invocation -> {
                    Movie movie = invocation.getArgument(0);
                    movie.setId(1L);
                    movie.setCreatedAt(createdDate);
                    return movie;
                });

        var actualResult = movieService.createMovie(request);

        verify(movieRepository).save(movieCaptor.capture());

        Movie moviePassedToRepository = movieCaptor.getValue();

        assertThat(moviePassedToRepository.getTitle()).isEqualTo("Matrix");
        assertThat(moviePassedToRepository.getDescription()).isEqualTo("AI");
        assertThat(moviePassedToRepository.getReleaseYear()).isEqualTo(1999);
        assertThat(moviePassedToRepository.getAgeRating()).isEqualTo("18+");
        assertThat(moviePassedToRepository.getDurationMinutes()).isEqualTo(132);
        assertThat(moviePassedToRepository.getStatus()).isEqualTo(MovieStatus.RELEASED);
        assertThat(moviePassedToRepository.getDirectorList()).isEmpty();
        assertThat(moviePassedToRepository.getActorList()).isEmpty();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
        verify(directorRepository, never()).findAllById(anyList());
        verify(actorRepository, never()).findAllById(anyList());
    }

    @Test
    void shouldCreateMovieWithOneActorAndCharacter() {
        LocalDateTime createdDate = LocalDateTime.now();
        Actor actor = formingActorEntity(6L, "Keanu", "Reeves");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of())
                .setActors(List.of(new MovieActorRequestDTO(6L, "Neo")));

        when(actorRepository.findAllById(List.of(6L))).thenReturn(List.of(actor));
        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(invocation -> {
                    Movie movie = invocation.getArgument(0);
                    movie.setId(1L);
                    movie.setCreatedAt(createdDate);
                    return movie;
                });

        MovieResponseDTO actualResult = movieService.createMovie(request);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        
        verify(actorRepository).findAllById(List.of(6L));
        verify(movieRepository).save(movieCaptor.capture());
        
        assertThat(movieCaptor.getValue().getActorList()).hasSize(1);
        MovieActor movieActor = movieCaptor.getValue().getActorList().getFirst();
        
        assertThat(movieActor.getMovie()).isSameAs(movieCaptor.getValue());
        assertThat(movieActor.getActor()).isSameAs(actor);
        assertThat(movieActor.getCharacterName()).isEqualTo("Neo");
        assertThat(actualResult.getActors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo"));
    }

    @Test
    void shouldCreateMovieWithMultipleActors() {
        LocalDateTime createdDate = LocalDateTime.now();
        Actor firstActor = formingActorEntity(6L, "Keanu", "Reeves");
        Actor secondActor = formingActorEntity(7L, "Laurence", "Fishburne");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of())
                .setActors(List.of(
                        new MovieActorRequestDTO(6L, "Neo"),
                        new MovieActorRequestDTO(7L, "Morpheus")
                ));

        when(actorRepository.findAllById(List.of(6L, 7L))).thenReturn(List.of(firstActor, secondActor));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            movie.setCreatedAt(createdDate);
            return movie;
        });

        MovieResponseDTO actualResult = movieService.createMovie(request);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        
        verify(actorRepository).findAllById(List.of(6L, 7L));
        verify(movieRepository).save(movieCaptor.capture());
        
        assertThat(movieCaptor.getValue().getActorList()).hasSize(2);
        assertThat(movieCaptor.getValue().getActorList().get(0).getActor()).isSameAs(firstActor);
        assertThat(movieCaptor.getValue().getActorList().get(0).getCharacterName()).isEqualTo("Neo");
        assertThat(movieCaptor.getValue().getActorList().get(1).getActor()).isSameAs(secondActor);
        assertThat(movieCaptor.getValue().getActorList().get(1).getCharacterName()).isEqualTo("Morpheus");
        assertThat(actualResult.getActors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                        new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo"),
                        new MovieActorResponseDTO(7L, "Laurence", "Fishburne", "Morpheus")
                );
    }

    @Test
    void shouldThrowExceptionWhenCreatingMovieWithUnknownActorId() {
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of())
                .setActors(List.of(new MovieActorRequestDTO(404L, "Unknown")));
        
        when(actorRepository.findAllById(List.of(404L))).thenReturn(List.of());

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldCreateMovieWithOneDirector() {
        LocalDateTime createdDate = LocalDateTime.now();
        Director director = formingDirectorEntity(4L, "Lana", "Wachowski");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of(4L));

        when(directorRepository.findAllById(List.of(4L))).thenReturn(List.of(director));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            movie.setCreatedAt(createdDate);
            return movie;
        });

        MovieResponseDTO actualResult = movieService.createMovie(request);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        verify(directorRepository).findAllById(List.of(4L));
        assertThat(movieCaptor.getValue().getDirectorList())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(director);
        assertThat(actualResult.getDirectors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieDirectorResponseDTO(4L, "Lana", "Wachowski"));
    }

    @Test
    void shouldCreateMovieWithMultipleDirectors() {
        LocalDateTime createdDate = LocalDateTime.now();
        Director firstDirector = formingDirectorEntity(4L, "Lana", "Wachowski");
        Director secondDirector = formingDirectorEntity(5L, "Lilly", "Wachowski");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of(4L, 5L));

        when(directorRepository.findAllById(List.of(4L, 5L)))
                .thenReturn(List.of(firstDirector, secondDirector));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            movie.setCreatedAt(createdDate);
            return movie;
        });

        MovieResponseDTO actualResult = movieService.createMovie(request);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        verify(directorRepository).findAllById(List.of(4L, 5L));
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue().getDirectorList())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(firstDirector, secondDirector);
        assertThat(actualResult.getDirectors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                        new MovieDirectorResponseDTO(4L, "Lana", "Wachowski"),
                        new MovieDirectorResponseDTO(5L, "Lilly", "Wachowski")
                );
    }

    @Test
    void shouldThrowExceptionWhenCreatingMovieWithUnknownDirectorId() {
        MovieRequestDTO request = formingMovieRequestDTO("Matrix", List.of(404L));

        when(directorRepository.findAllById(List.of(404L))).thenReturn(List.of());

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldUpdateMovieAndReplaceActorsWithMultipleActors() {
        Movie movie = new Movie(
                1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED
        );

        Actor oldActor = formingActorEntity(5L, "Old", "Actor");
        MovieActor oldMovieActor = new MovieActor()
                .setMovie(movie)
                .setActor(oldActor)
                .setCharacterName("Old Character");

        movie.setActorList(new ArrayList<>(List.of(oldMovieActor)));

        Actor firstActor = formingActorEntity(6L, "Keanu", "Reeves");
        Actor secondActor = formingActorEntity(7L, "Laurence", "Fishburne");

        MovieRequestDTO request = formingMovieRequestDTO("Matrix-2", List.of())
                .setActors(List.of(
                        new MovieActorRequestDTO(6L, "Neo"),
                        new MovieActorRequestDTO(7L, "Morpheus")
                ));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(actorRepository.findAllById(List.of(6L, 7L)))
                .thenReturn(List.of(firstActor, secondActor));

        MovieResponseDTO result = movieService.updateMovie(1L, request);

        assertThat(movie.getActorList()).hasSize(2);

        assertThat(movie.getActorList())
                .extracting(MovieActor::getActor)
                .containsExactly(firstActor, secondActor);

        assertThat(movie.getActorList())
                .extracting(MovieActor::getCharacterName)
                .containsExactly("Neo", "Morpheus");

        assertThat(movie.getActorList())
                .allSatisfy(movieActor ->
                        assertThat(movieActor.getMovie()).isSameAs(movie)
                );

        assertThat(result.getActors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                        new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo"),
                        new MovieActorResponseDTO(7L, "Laurence", "Fishburne", "Morpheus")
                );

        verify(actorRepository).findAllById(List.of(6L, 7L));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMovieWithUnknownActorId() {
        Movie movie = new Movie(
                1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED
        );

        Actor existingActor = formingActorEntity(5L, "Old", "Actor");
        MovieActor existingMovieActor = new MovieActor()
                .setMovie(movie)
                .setActor(existingActor)
                .setCharacterName("Old Character");

        movie.setActorList(new ArrayList<>(List.of(existingMovieActor)));

        MovieRequestDTO request = formingMovieRequestDTO("Matrix-2", List.of())
                .setActors(List.of(new MovieActorRequestDTO(404L, "Unknown")));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(actorRepository.findAllById(List.of(404L))).thenReturn(List.of());

        assertThatThrownBy(() -> movieService.updateMovie(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");

        assertThat(movie.getActorList()).containsExactly(existingMovieActor);

        verify(actorRepository).findAllById(List.of(404L));
        verify(movieRepository, never()).save(any(Movie.class));
    }
    
    @Test
    void shouldUpdateMovieAndReplaceDirectors() {
        LocalDateTime createdDate = LocalDateTime.now();
        Director existingDirector = formingDirectorEntity(3L, "Existing", "Director");
        Movie existingMovie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate);
        existingMovie.setDirectorList(List.of(existingDirector));
        Director replacementDirector = formingDirectorEntity(4L, "Lana", "Wachowski");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix-2", List.of(4L));
        MovieResponseDTO expectedResult = formingMovieResponseDTO(1L, "Matrix-2", 1999, createdDate,
                List.of(new MovieDirectorResponseDTO(4L, "Lana", "Wachowski")), List.of());

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(existingMovie));
        when(directorRepository.findAllById(List.of(4L))).thenReturn(List.of(replacementDirector));

        var actualResult = movieService.updateMovie(1L, request);

        verify(movieRepository, never()).save(any(Movie.class));
        verify(movieRepository).findById(1L);
        assertThat(existingMovie.getDirectorList()).containsExactly(replacementDirector);
        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldUpdateMovieAndReplaceActors() {
        LocalDateTime createdDate = LocalDateTime.now();
        Movie movie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate);
        Actor oldActor = formingActorEntity(5L, "Old", "Actor");
        MovieActor oldMovieActor = new MovieActor()
                .setMovie(movie)
                .setActor(oldActor)
                .setCharacterName("Old Character");
        movie.setActorList(new ArrayList<>(List.of(oldMovieActor)));

        Actor replacementActor = formingActorEntity(6L, "Keanu", "Reeves");
        MovieRequestDTO request = formingMovieRequestDTO("Matrix-2", List.of())
                .setActors(List.of(new MovieActorRequestDTO(6L, "Neo")));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(actorRepository.findAllById(List.of(6L))).thenReturn(List.of(replacementActor));

        MovieResponseDTO actualResult = movieService.updateMovie(1L, request);

        verify(movieRepository, never()).save(any(Movie.class));
        verify(actorRepository).findAllById(List.of(6L));
        assertThat(movie.getActorList()).hasSize(1);
        MovieActor updatedMovieActor = movie.getActorList().getFirst();
        assertThat(updatedMovieActor).isNotSameAs(oldMovieActor);
        assertThat(updatedMovieActor.getMovie()).isSameAs(movie);
        assertThat(updatedMovieActor.getActor()).isSameAs(replacementActor);
        assertThat(updatedMovieActor.getCharacterName()).isEqualTo("Neo");
        assertThat(actualResult.getActors())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new MovieActorResponseDTO(6L, "Keanu", "Reeves", "Neo"));
    }

    @Test
    void shouldClearMovieActorsWhenUpdatingWithEmptyActors() {
        Movie movie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        Actor actor = formingActorEntity(6L, "Keanu", "Reeves");
        movie.setActorList(new ArrayList<>(List.of(
                new MovieActor().setMovie(movie).setActor(actor).setCharacterName("Neo")
        )));
        
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieResponseDTO actualResult = movieService.updateMovie(
                1L,
                formingMovieRequestDTO("Matrix", List.of()).setActors(List.of())
        );

        assertThat(movie.getActorList()).isEmpty();
        assertThat(actualResult.getActors()).isEmpty();
        verify(actorRepository, never()).findAllById(anyList());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldClearMovieDirectorsWhenUpdatingWithEmptyDirectors() {
        LocalDateTime createdDate = LocalDateTime.now();
        Movie movie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED, createdDate);
        movie.setDirectorList(List.of(formingDirectorEntity(3L, "Existing", "Director")));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieResponseDTO actualResult = movieService.updateMovie(1L, formingMovieRequestDTO("Matrix", List.of()));

        assertThat(movie.getDirectorList()).isEmpty();
        assertThat(actualResult.getDirectors()).isEmpty();
        verify(directorRepository, never()).findAllById(anyList());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMovie() {
        MovieRequestDTO request = new MovieRequestDTO("Matrix-2", "AI", 1999, 132, "18+", MovieStatus.RELEASED, null);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.updateMovie(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Movie not found with id: " + 1L);

        verify(movieRepository).findById(1L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldDeleteMovie() {
        Movie movie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        movieService.deleteMovie(1L);

        verify(movieRepository).findById(1L);
        verify(movieRepository).delete(movie);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentMovie() {
        when(movieRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Movie not found with id: " + 1L);

        verify(movieRepository).findById(1L);
        verify(movieRepository, never()).delete(any(Movie.class));
    }

    private MovieRequestDTO formingMovieRequestDTO(String title, List<Long> directorIds) {
        return new MovieRequestDTO(title, "AI", 1999, 132, "18+", MovieStatus.RELEASED, directorIds);
    }

    private MovieResponseDTO formingMovieResponseDTO(Long id, String title, Integer releaseYear, LocalDateTime createdAt, List<MovieDirectorResponseDTO> directors, List<MovieActorResponseDTO> actors) {
        return new MovieResponseDTO(id, title, "AI", releaseYear, 132, "18+", MovieStatus.RELEASED, createdAt)
                .setDirectors(directors)
                .setActors(actors);
    }

    private Director formingDirectorEntity(Long id, String firstName, String lastName) {
        return new Director(id, firstName, lastName, null, "USA");
    }

    private Actor formingActorEntity(Long id, String firstName, String lastName) {
        return new Actor(id, firstName, lastName, null, "USA");
    }
}
