package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.DirectorSummaryDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.mapper.DirectorMapper;
import com.moviecatalog.movie_catalog.mapper.MovieMapper;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieMapper movieMapper;
    DirectorMapper directorMapper;
    MovieRepository movieRepository;
    DirectorRepository directorRepository;

    public MovieService(MovieMapper movieMapper,
                        DirectorMapper directorMapper,
                        MovieRepository movieRepository,
                        DirectorRepository directorRepository) {
        this.movieMapper = movieMapper;
        this.directorMapper = directorMapper;
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
    }

    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        MovieResponseDTO response = movieRepository.findMovieById(id)
                .map(movieMapper::toMovieResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));

        response.setDirectors(movieRepository.findDirectorsByMovieId(id).stream()
                .map(directorMapper::toDirectorSummaryDTO)
                .toList());

        return response;
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getAllMovies() {
        List<MovieResponseDTO> responses = movieRepository.findAllMovies()
                .stream()
                .map(movieMapper::toMovieResponseDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        if (responses.isEmpty())
            return responses;

        List<Long> movieIds = responses.stream()
                .map(MovieResponseDTO::getId)
                .distinct()
                .toList();
        
        Map<Long, List<DirectorSummaryDTO>> movieIdDirectorsMap = movieRepository.findDirectorsByMovieIds(movieIds)
                .stream()
                .collect(Collectors.groupingBy(
                        MovieDirectorDTO::getMovieId,
                        LinkedHashMap::new,
                        Collectors.mapping(directorMapper::toDirectorSummaryDTO, Collectors.toList())
                ));

        responses.forEach(el -> el.setDirectors(
                movieIdDirectorsMap.getOrDefault(el.getId(), List.of())
        ));
        
        return responses;
    }

    @Transactional
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie newMovie = movieMapper.toMovie(movieRequestDTO);
        newMovie.setDirectorList(findDirectors(movieRequestDTO.getDirectorIds()));
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
        
        movie.setDirectorList(findDirectors(movieRequestDTO.getDirectorIds()));

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

    private List<Director> findDirectors(List<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty())
            return new ArrayList<>();

        List<Long> distinctIds = directorIds.stream()
                .distinct()
                .toList();
        
        Map<Long, Director> directorsById = directorRepository.findAllById(distinctIds)
                .stream()
                .collect(Collectors.toMap(Director::getId, Function.identity()));

        List<Long> missingIds = distinctIds.stream()
                .filter(directorId -> !directorsById.containsKey(directorId))
                .toList();
        
        if (!missingIds.isEmpty()) 
            throw new EntityNotFoundException("Director(s) not found with id(s): " + missingIds);

        return distinctIds.stream()
                .map(directorsById::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
