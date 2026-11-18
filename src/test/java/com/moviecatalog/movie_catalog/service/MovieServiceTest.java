package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.enums.MovieStatus;
import com.moviecatalog.movie_catalog.mapper.MovieMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {
    
    @Mock
    private MovieRepository movieRepository;
    
    @Spy
    private MovieMapper movieMapper = Mappers.getMapper(MovieMapper.class);
    
    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldReturnCorrectMovieById() {
        MovieDTO movieDTO = new MovieDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        MovieResponseDTO expectedResult = new MovieResponseDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        
        when(movieRepository.findMovieById(1L))
                .thenReturn(Optional.of(movieDTO));
        
        var actualResult = movieService.getMovieById(1L);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        when(movieRepository.findMovieById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Movie not found with id: " + 1L);
    }

    @Test
    void shouldReturnCorrectAllMovie() {
        MovieDTO movieDTO1 = new MovieDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        MovieDTO movieDTO2 = new MovieDTO(2L, "Matrix-2", "AI", 2003, 132, "18+", MovieStatus.RELEASED);
        List<MovieDTO> repositoryResponse = List.of(movieDTO1, movieDTO2);

        MovieResponseDTO movieResponseDTO1 = new MovieResponseDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        MovieResponseDTO movieResponseDTO2 = new MovieResponseDTO(2L, "Matrix-2", "AI", 2003, 132, "18+", MovieStatus.RELEASED);
        List<MovieResponseDTO> expectedResult = List.of(movieResponseDTO1, movieResponseDTO2);

        when(movieRepository.findAllMovies())
                .thenReturn(repositoryResponse);

        var actualResult = movieService.getAllMovies();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.size()).isEqualTo(2);
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulCreateMovie() {
        MovieRequestDTO request = new MovieRequestDTO("Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        MovieResponseDTO expectedResult = new MovieResponseDTO(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);

        when(movieRepository.save(any(Movie.class)))
                .thenReturn(new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED));

        var actualResult = movieService.createMovie(request);

        verify(movieRepository).save(movieCaptor.capture());

        Movie moviePassedToRepository = movieCaptor.getValue();

        assertThat(moviePassedToRepository.getTitle()).isEqualTo("Matrix");
        assertThat(moviePassedToRepository.getDescription()).isEqualTo("AI");
        assertThat(moviePassedToRepository.getReleaseYear()).isEqualTo(1999);
        assertThat(moviePassedToRepository.getAgeRating()).isEqualTo("18+");
        assertThat(moviePassedToRepository.getDurationMinutes()).isEqualTo(132);
        assertThat(moviePassedToRepository.getStatus()).isEqualTo(MovieStatus.RELEASED);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldSuccessfulUpdateMovie() {
        MovieRequestDTO request = new MovieRequestDTO("Matrix-2", "AI", 1999, 132, "18+", MovieStatus.RELEASED);
        MovieResponseDTO expectedResult = new MovieResponseDTO(1L, "Matrix-2", "AI", 1999, 132, "18+", MovieStatus.RELEASED);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED)));

        var actualResult = movieService.updateMovie(1L, request);

        verify(movieRepository, never()).save(any(Movie.class));

        verify(movieRepository).findById(1L);
        assertThat(actualResult).isNotNull();
        assertThat(actualResult)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMovie() {
        MovieRequestDTO request = new MovieRequestDTO("Matrix-2", "AI", 1999, 132, "18+", MovieStatus.RELEASED);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.updateMovie(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Movie not found with id: " + 1L);

        verify(movieRepository).findById(1L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldSuccessfulDeleteMovie() {
        Movie movie = new Movie(1L, "Matrix", "AI", 1999, 132, "18+", MovieStatus.RELEASED);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        movieService.deleteMovie(1L);

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
}