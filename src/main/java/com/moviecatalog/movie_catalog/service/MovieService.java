package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.mapper.MovieMapper;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {
    
    MovieMapper movieMapper;
    MovieRepository movieRepository;

    public MovieService(MovieMapper movieMapper, MovieRepository movieRepository) {
        this.movieMapper = movieMapper;
        this.movieRepository = movieRepository;
    }
    
    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        return movieRepository.findMovieById(id)
                .map(movieMapper::toMovieResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getAllMovies() {
        return movieRepository.findAllMovies()
                .stream()
                .map(movieMapper::toMovieResponseDTO)
                .toList();
    }

    @Transactional
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie newMovie = movieMapper.toMovie(movieRequestDTO);
        Movie savedMovie = movieRepository.save(newMovie);
        return movieMapper.toMovieResponseDTO(savedMovie);
    }
    
    @Transactional
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO movieRequestDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));
        
        movieMapper.updateMovie(movieRequestDTO, movie);
        
        return movieMapper.toMovieResponseDTO(movie);
    }
    
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));

        movieRepository.delete(movie);
    }
}
